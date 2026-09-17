package users.security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import org.eclipse.microprofile.jwt.JsonWebToken;
import users.domain.exception.UsuarioNoEncontradoException;
import users.domain.model.Rol;
import users.domain.model.Usuario;
import users.domain.model.UsuarioSedeRol;
import users.domain.repository.AdministrativoPerfilRepository;
import users.domain.repository.UsuarioRepository;
import users.domain.repository.UsuarioSedeRolRepository;

import java.util.List;
import java.util.UUID;

/**
 * Se ejecuta una vez por request (RequestScoped): resuelve el usuario local a
 * partir del sub del JWT y arma ContextoAcceso a partir de sus asignaciones
 * vigentes en UsuarioSedeRol.
 */
@RequestScoped
public class ContextoAccesoProvider {

  @Inject
  JsonWebToken jwt;

  @Inject
  UsuarioRepository usuarioRepository;

  @Inject
  UsuarioSedeRolRepository usuarioSedeRolRepository;

  @Inject
  AdministrativoPerfilRepository administrativoPerfilRepository;

  @Produces
  @RequestScoped
  public ContextoAcceso contextoAcceso() {
    if (jwt == null || jwt.getName() == null) {
      throw new NotAuthorizedException("No se encontró token de autenticación válido");
    }

    // 1. Intentar resolver el usuario por keycloakId (sub) o por DNI (preferred_username)
    Usuario usuario;
    String subject = jwt.getSubject();

    if (subject != null && !subject.isBlank()) {
      UUID keycloakId = UUID.fromString(subject);
      usuario = usuarioRepository.buscarPorKeycloakId(keycloakId)
              .orElseThrow(() -> UsuarioNoEncontradoException.porKeycloakId(keycloakId.toString()));
    } else {
      // Si el token no tiene claim 'sub', buscamos por 'preferred_username' (DNI)
      String username = jwt.getClaim("preferred_username");
      if (username == null || username.isBlank()) {
        throw new NotAuthorizedException("El token no cuenta con claim 'sub' ni 'preferred_username'");
      }
      usuario = usuarioRepository.find("numeroDocumento", username)
              .firstResultOptional()
              .orElseThrow(() -> new NotAuthorizedException("Usuario no encontrado con documento: " + username));
    }

    // 2. El resto de tu lógica permanece exactamente igual:
    List<UsuarioSedeRol> asignacionesVigentes = usuarioSedeRolRepository.vigentesDe(usuario.id);

    boolean adminGlobal = asignacionesVigentes.stream()
            .anyMatch(usr -> usr.rol == Rol.ADMIN && usr.esAccesoGlobal());

    Rol rol = asignacionesVigentes.stream()
            .findFirst()
            .map(usr -> usr.rol)
            .orElse(null);

    List<Long> sedesPermitidas = adminGlobal
            ? null
            : asignacionesVigentes.stream().map(usr -> usr.sedeId).toList();

    List<Rol> roles = asignacionesVigentes.stream()
            .map(usr -> usr.rol)
            .distinct()
            .toList();

    Long cargoId = administrativoPerfilRepository.buscarPorUsuarioId(usuario.id)
            .map(perfil -> perfil.cargo.id)
            .orElse(null);

    return new ContextoAcceso(usuario.id, rol, roles, sedesPermitidas, adminGlobal, cargoId);
  }
}