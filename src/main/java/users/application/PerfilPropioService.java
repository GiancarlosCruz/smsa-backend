package users.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import users.api.generated.model.ActualizarPerfilPropioRequest;
import users.api.generated.model.PerfilDocente;
import users.api.generated.model.PerfilEstudiante;
import users.api.generated.model.UsuarioPerfilResponse;
import users.domain.exception.UsuarioNoEncontradoException;
import users.domain.model.Rol;
import users.domain.model.Usuario;
import users.domain.repository.DocentePerfilRepository;
import users.domain.repository.EstudiantePerfilRepository;
import users.domain.repository.UsuarioRepository;
import users.infrastructure.keycloak.KeycloakProvisioningService;
import users.security.ContextoAcceso;

@ApplicationScoped
public class PerfilPropioService {

  @Inject UsuarioRepository usuarioRepository;
  @Inject DocentePerfilRepository docentePerfilRepository;
  @Inject EstudiantePerfilRepository estudiantePerfilRepository;
  @Inject KeycloakProvisioningService keycloakProvisioningService;

  public UsuarioPerfilResponse obtenerPropio(ContextoAcceso contexto) {
    Usuario usuario = usuarioRepository.findByIdOptional(contexto.usuarioId())
            .orElseThrow(() -> new UsuarioNoEncontradoException(contexto.usuarioId()));

    UsuarioPerfilResponse response = new UsuarioPerfilResponse();
    response.setId(usuario.id);
    response.setNombres(usuario.nombres);
    response.setApellidoPaterno(usuario.apellidoPaterno);
    response.setApellidoMaterno(usuario.apellidoMaterno);
    response.setEmail(usuario.email);
    response.setTelefono(usuario.telefono);
    response.setEstado(users.api.generated.model.EstadoUsuario.valueOf(usuario.estado.name()));
    response.setRol(users.api.generated.model.Rol.valueOf(contexto.rol().name()));
    response.setSedes(contexto.sedesPermitidas());

    if (contexto.rol() == Rol.DOCENTE) {
      docentePerfilRepository.buscarPorUsuarioId(usuario.id).ifPresent(dp -> {
        PerfilDocente dto = new PerfilDocente();
        dto.setGradoAcademico(dp.gradoAcademico);
        dto.setEspecialidad(dp.especialidad);
        dto.setTipoContrato(dp.tipoContrato);
        dto.setFechaIngreso(dp.fechaIngreso);
        response.setPerfilDocente(dto);
      });
    }

    if (contexto.rol() == Rol.ESTUDIANTE) {
      estudiantePerfilRepository.buscarPorUsuarioId(usuario.id).ifPresent(ep -> {
        PerfilEstudiante dto = new PerfilEstudiante();
        dto.setCodigoEstudiante(ep.codigoEstudiante);
        dto.setProgramaId(ep.programaId);
        dto.setContactoEmergenciaNombre(ep.contactoEmergenciaNombre);
        dto.setContactoEmergenciaTelefono(ep.contactoEmergenciaTelefono);
        dto.setDireccion(ep.direccion);
        response.setPerfilEstudiante(dto);
      });
    }

    return response;
  }

  /**
   * Solo campos editables por el propio usuario (ver ActualizarPerfilPropioRequest
   * en el contrato): NUNCA rol, sede o documento de identidad desde este método.
   */
  @Transactional
  public void actualizarPropio(ContextoAcceso contexto, ActualizarPerfilPropioRequest request) {
    Usuario usuario = usuarioRepository.findByIdOptional(contexto.usuarioId())
            .orElseThrow(() -> new UsuarioNoEncontradoException(contexto.usuarioId()));

    if (request.getTelefono() != null) {
      usuario.telefono = request.getTelefono();
    }

    if (contexto.rol() == Rol.ESTUDIANTE) {
      estudiantePerfilRepository.buscarPorUsuarioId(usuario.id).ifPresent(perfil -> {
        if (request.getDireccion() != null) {
          perfil.direccion = request.getDireccion();
        }
        if (request.getContactoEmergenciaNombre() != null) {
          perfil.contactoEmergenciaNombre = request.getContactoEmergenciaNombre();
        }
        if (request.getContactoEmergenciaTelefono() != null) {
          perfil.contactoEmergenciaTelefono = request.getContactoEmergenciaTelefono();
        }
      });
    }
  }

  /**
   * Self-service: no requiere la contraseña anterior. Es un trade-off
   * consciente por simplicidad — el usuario ya está autenticado con un JWT
   * válido, que es la misma confianza que usamos para el resto de acciones
   * sobre "mi propio perfil". Si más adelante se requiere mayor rigor,
   * se puede exigir reautenticación (login_hint/max_age) antes de este paso.
   */
  public void cambiarPasswordPropio(ContextoAcceso contexto, String passwordNueva) {
    Usuario usuario = usuarioRepository.findByIdOptional(contexto.usuarioId())
            .orElseThrow(() -> new UsuarioNoEncontradoException(contexto.usuarioId()));
    keycloakProvisioningService.cambiarPassword(usuario.keycloakId, passwordNueva);
  }
}