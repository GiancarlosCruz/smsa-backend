package users.security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
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
    UUID keycloakId = UUID.fromString(jwt.getSubject());

    Usuario usuario = usuarioRepository.buscarPorKeycloakId(keycloakId)
            .orElseThrow(() -> UsuarioNoEncontradoException.porKeycloakId(keycloakId.toString()));

    List<UsuarioSedeRol> asignacionesVigentes = usuarioSedeRolRepository.vigentesDe(usuario.id);

    boolean adminGlobal = asignacionesVigentes.stream()
            .anyMatch(usr -> usr.rol == Rol.ADMIN && usr.esAccesoGlobal());

    // El rol efectivo ya viene validado por @RolesAllowed contra el JWT;
    // esto es solo informativo para el resto del contexto de negocio.
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