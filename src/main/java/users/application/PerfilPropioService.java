package users.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import users.api.generated.model.ActualizarPerfilPropioRequest;
import users.api.generated.model.PerfilAdministrativo;
import users.api.generated.model.PerfilDocente;
import users.api.generated.model.PerfilEstudiante;
import users.api.generated.model.UsuarioPerfilResponse;
import users.domain.exception.UsuarioNoEncontradoException;
import users.domain.model.Usuario;
import users.domain.repository.AdministrativoPerfilRepository;
import users.domain.repository.DocentePerfilRepository;
import users.domain.repository.EstudiantePerfilRepository;
import users.domain.repository.UsuarioRepository;
import users.domain.repository.UsuarioSedeRolRepository;
import users.infrastructure.keycloak.KeycloakProvisioningService;
import users.security.ContextoAcceso;

@ApplicationScoped
public class PerfilPropioService {

  private final UsuarioRepository usuarioRepository;
  private final UsuarioSedeRolRepository usuarioSedeRolRepository;
  private final DocentePerfilRepository docentePerfilRepository;
  private final EstudiantePerfilRepository estudiantePerfilRepository;
  private final AdministrativoPerfilRepository administrativoPerfilRepository;
  private final KeycloakProvisioningService keycloakProvisioningService;

  @Inject
  public PerfilPropioService(UsuarioRepository usuarioRepository,
                             UsuarioSedeRolRepository usuarioSedeRolRepository,
                             DocentePerfilRepository docentePerfilRepository,
                             EstudiantePerfilRepository estudiantePerfilRepository,
                             AdministrativoPerfilRepository administrativoPerfilRepository,
                             KeycloakProvisioningService keycloakProvisioningService) {
    this.usuarioRepository = usuarioRepository;
    this.usuarioSedeRolRepository = usuarioSedeRolRepository;
    this.docentePerfilRepository = docentePerfilRepository;
    this.estudiantePerfilRepository = estudiantePerfilRepository;
    this.administrativoPerfilRepository = administrativoPerfilRepository;
    this.keycloakProvisioningService = keycloakProvisioningService;

  }

  /**
   * Se consultan los tres tipos de perfil SIEMPRE, sin filtrar por
   * contexto.rol() — ese campo solo guarda el primer rol que se encontró
   * en usuario_sede_rol (es "informativo", ver ContextoAccesoProvider), y
   * un usuario puede legítimamente tener más de un rol (ej. DOCENTE +
   * ADMINISTRATIVO). Filtrar por ese único valor ocultaría el segundo
   * perfil aunque exista en la BD. Cada Optional simplemente viene vacío
   * si esa persona no tiene ese perfil, sin necesidad de preguntar antes
   * "qué roles tiene".
   */
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
    response.setSexo(usuario.sexo != null
            ? users.api.generated.model.Sexo.valueOf(usuario.sexo) : null);
    response.setFechaNacimiento(usuario.fechaNacimiento);
    response.setEstado(users.api.generated.model.EstadoUsuario.valueOf(usuario.estado.name()));
    response.setRol(users.api.generated.model.Rol.valueOf(contexto.rol().name()));
    response.setRoles(
            usuarioSedeRolRepository.vigentesDe(usuario.id).stream()
                    .map(usr -> users.api.generated.model.Rol.valueOf(usr.rol.name()))
                    .distinct()
                    .toList());
    response.setSedes(contexto.sedesPermitidas());

    docentePerfilRepository.buscarPorUsuarioId(usuario.id).ifPresent(dp -> {
      PerfilDocente dto = new PerfilDocente();
      dto.setGradoAcademico(dp.gradoAcademico);
      dto.setEspecialidad(dp.especialidad);
      dto.setTipoContrato(dp.tipoContrato);
      dto.setFechaIngreso(dp.fechaIngreso);
      dto.setDireccion(dp.direccion);
      dto.setCargoAdministrativo(dp.cargoAdministrativo);
      response.setPerfilDocente(dto);
    });

    estudiantePerfilRepository.buscarPorUsuarioId(usuario.id).ifPresent(ep -> {
      PerfilEstudiante dto = new PerfilEstudiante();
      dto.setCodigoEstudiante(ep.codigoEstudiante);
      dto.setProgramaId(ep.programaId);
      dto.setContactoEmergenciaNombre(ep.contactoEmergenciaNombre);
      dto.setContactoEmergenciaTelefono(ep.contactoEmergenciaTelefono);
      dto.setDireccion(ep.direccion);
      response.setPerfilEstudiante(dto);
    });

    administrativoPerfilRepository.buscarPorUsuarioId(usuario.id).ifPresent(ap -> {
      PerfilAdministrativo dto = new PerfilAdministrativo();
      dto.setCargoId(ap.cargo.id);
      dto.setCargoNombre(ap.cargo.nombre);
      dto.setAreaAdministrativa(ap.areaAdministrativa);
      dto.setCondicion(ap.condicion);
      response.setPerfilAdministrativo(dto);
    });

    return response;
  }

  /**
   * Solo campos editables por el propio usuario (ver ActualizarPerfilPropioRequest
   * en el contrato): NUNCA rol, sede o documento de identidad desde este método.
   * Igual que en obtenerPropio, no se filtra por contexto.rol() — si la
   * persona tiene EstudiantePerfil y/o DocentePerfil (dirección vive en
   * ambos), se actualiza el que exista, no solo el del rol "informativo".
   */
  @Transactional
  public void actualizarPropio(ContextoAcceso contexto, ActualizarPerfilPropioRequest request) {
    Usuario usuario = usuarioRepository.findByIdOptional(contexto.usuarioId())
            .orElseThrow(() -> new UsuarioNoEncontradoException(contexto.usuarioId()));

    if (request.getTelefono() != null) {
      usuario.telefono = request.getTelefono();
    }

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

    docentePerfilRepository.buscarPorUsuarioId(usuario.id).ifPresent(perfil -> {
      if (request.getDireccion() != null) {
        perfil.direccion = request.getDireccion();
      }
    });
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