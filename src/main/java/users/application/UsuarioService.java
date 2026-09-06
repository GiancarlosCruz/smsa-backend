package users.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import users.api.generated.model.ActualizarUsuarioRequest;
import users.api.generated.model.CrearUsuarioRequest;
import users.domain.exception.AccesoSedeNoPermitidoException;
import users.domain.exception.DocumentoDuplicadoException;
import users.domain.exception.EmailDuplicadoException;
import users.domain.exception.UsuarioNoEncontradoException;
import users.domain.model.AdministrativoPerfil;
import users.domain.model.DocentePerfil;
import users.domain.model.EstadoUsuario;
import users.domain.model.EstudiantePerfil;
import users.domain.model.Rol;
import users.domain.model.Usuario;
import users.domain.model.UsuarioSedeRol;
import users.domain.repository.AdministrativoPerfilRepository;
import users.domain.repository.CargoRepository;
import users.domain.repository.DocentePerfilRepository;
import users.domain.repository.EstudiantePerfilRepository;
import users.domain.repository.UsuarioRepository;
import users.domain.repository.UsuarioSedeRolRepository;
import users.infrastructure.keycloak.KeycloakProvisioningService;
import users.security.ContextoAcceso;

import java.time.LocalDate;
import java.util.UUID;

@ApplicationScoped
public class UsuarioService {

  private static final Logger LOG = Logger.getLogger(UsuarioService.class);

  @Inject UsuarioRepository usuarioRepository;
  @Inject UsuarioSedeRolRepository usuarioSedeRolRepository;
  @Inject DocentePerfilRepository docentePerfilRepository;
  @Inject EstudiantePerfilRepository estudiantePerfilRepository;
  @Inject AdministrativoPerfilRepository administrativoPerfilRepository;
  @Inject CargoRepository cargoRepository;
  @Inject KeycloakProvisioningService keycloakProvisioningService;

  /**
   * Keycloak primero (fuera de la transacción local, es una llamada de red
   * a un sistema externo), luego la escritura local ya transaccional.
   * Si la escritura local falla, se compensa eliminando lo creado en Keycloak.
   */
  public Usuario crear(CrearUsuarioRequest request, ContextoAcceso contexto) {
    if (usuarioRepository.existeDocumento(request.getTipoDocumento(), request.getNumeroDocumento())) {
      throw new DocumentoDuplicadoException(request.getTipoDocumento(), request.getNumeroDocumento());
    }

    if(usuarioRepository.existeEmail(request.getEmail())) {
      throw new EmailDuplicadoException(request.getEmail());
    }

    Rol rol = Rol.valueOf(request.getRol().name());

    if (!contexto.esAdminGlobal() && !contexto.tieneAccesoASede(request.getSedeId())) {
      throw new AccesoSedeNoPermitidoException(request.getSedeId());
    }

    UUID keycloakId = keycloakProvisioningService.crearUsuario(
            request.getNumeroDocumento(), request.getEmail(), request.getNombres(), request.getApellidoPaterno(), rol);

    try {
      return persistirUsuarioNuevo(request, rol, keycloakId);
    } catch (RuntimeException e) {
      LOG.errorf(e, "Falló la escritura local tras crear en Keycloak (keycloakId=%s), compensando", keycloakId);
      keycloakProvisioningService.eliminarPorId(keycloakId);
      throw e;
    }
  }

  @Transactional
  protected Usuario persistirUsuarioNuevo(CrearUsuarioRequest request, Rol rol, UUID keycloakId) {
    Usuario usuario = new Usuario();
    usuario.keycloakId = keycloakId;
    usuario.tipoDocumento = request.getTipoDocumento();
    usuario.numeroDocumento = request.getNumeroDocumento();
    usuario.nombres = request.getNombres();
    usuario.apellidoPaterno = request.getApellidoPaterno();
    usuario.apellidoMaterno = request.getApellidoMaterno();
    usuario.email = request.getEmail();
    usuario.telefono = request.getTelefono();
    usuario.sexo = request.getSexo() != null
            ? users.domain.model.Sexo.valueOf(request.getSexo().name()).name()
            : null;
    usuario.fechaNacimiento = request.getFechaNacimiento();
    usuario.estado = EstadoUsuario.ACTIVO;
    usuarioRepository.persist(usuario);

    UsuarioSedeRol asignacion = new UsuarioSedeRol();
    asignacion.usuario = usuario;
    // sedeId null solo tiene sentido para ADMIN (acceso global); RolSedeService
    // es quien valida esa regla en el flujo de asignación de roles posteriores.
    asignacion.sedeId = request.getSedeId();
    asignacion.rol = rol;
    asignacion.fechaInicio = LocalDate.now();
    usuarioSedeRolRepository.persist(asignacion);

    if (rol == Rol.DOCENTE && request.getPerfilDocente() != null) {
      var dto = request.getPerfilDocente();
      DocentePerfil perfil = new DocentePerfil();
      perfil.usuario = usuario;
      perfil.gradoAcademico = dto.getGradoAcademico();
      perfil.especialidad = dto.getEspecialidad();
      perfil.tipoContrato = dto.getTipoContrato();
      perfil.fechaIngreso = dto.getFechaIngreso();
      docentePerfilRepository.persist(perfil);
    }

    if (rol == Rol.ESTUDIANTE && request.getPerfilEstudiante() != null) {
      var dto = request.getPerfilEstudiante();
      EstudiantePerfil perfil = new EstudiantePerfil();
      perfil.usuario = usuario;
      perfil.codigoEstudiante = dto.getCodigoEstudiante();
      perfil.programaId = dto.getProgramaId();
      perfil.contactoEmergenciaNombre = dto.getContactoEmergenciaNombre();
      perfil.contactoEmergenciaTelefono = dto.getContactoEmergenciaTelefono();
      perfil.direccion = dto.getDireccion();
      estudiantePerfilRepository.persist(perfil);
    }

    if (rol == Rol.ADMINISTRATIVO && request.getPerfilAdministrativo() != null) {
      var dto = request.getPerfilAdministrativo();
      AdministrativoPerfil perfil = new AdministrativoPerfil();
      perfil.usuario = usuario;
      perfil.cargo = cargoRepository.findByIdOptional(dto.getCargoId())
              .orElseThrow(() -> new IllegalArgumentException("cargoId inexistente: " + dto.getCargoId()));
      perfil.areaAdministrativa = dto.getAreaAdministrativa();
      perfil.condicion = dto.getCondicion();
      administrativoPerfilRepository.persist(perfil);
    }

    return usuario;
  }

  @Transactional
  public Usuario actualizar(Long usuarioId, ActualizarUsuarioRequest request, ContextoAcceso contexto) {
    Usuario usuario = usuarioRepository.findByIdOptional(usuarioId)
            .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));

    validarAccesoSobreUsuario(usuario, contexto);

    if (request.getNombres() != null) usuario.nombres = request.getNombres();
    if (request.getApellidoPaterno() != null) usuario.apellidoPaterno = request.getApellidoPaterno();
    if (request.getTelefono() != null) usuario.telefono = request.getTelefono();

    keycloakProvisioningService.actualizarDatosBasicos(
            usuario.keycloakId, usuario.nombres, usuario.apellidoPaterno, usuario.email);

    return usuario;
  }

  @Transactional
  public void cambiarEstado(Long usuarioId, EstadoUsuario nuevoEstado, ContextoAcceso contexto) {
    Usuario usuario = usuarioRepository.findByIdOptional(usuarioId)
            .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));

    validarAccesoSobreUsuario(usuario, contexto);

    usuario.estado = nuevoEstado;
    keycloakProvisioningService.cambiarHabilitado(usuario.keycloakId, nuevoEstado == EstadoUsuario.ACTIVO);
  }

  /**
   * El admin necesita acceso a AL MENOS UNA sede vigente del usuario objetivo.
   * Usuario no tiene sede_id propia, por eso se consulta UsuarioSedeRol.
   */
  private void validarAccesoSobreUsuario(Usuario usuario, ContextoAcceso contexto) {
    if (contexto.esAdminGlobal()) {
      return;
    }
    var asignacionesVigentes = usuarioSedeRolRepository.vigentesDe(usuario.id);
    boolean tieneAcceso = asignacionesVigentes.stream()
            .anyMatch(usr -> contexto.tieneAccesoASede(usr.sedeId));
    if (!tieneAcceso) {
      Long sedeReferencia = asignacionesVigentes.stream().findFirst().map(usr -> usr.sedeId).orElse(null);
      throw new AccesoSedeNoPermitidoException(sedeReferencia);
    }
  }
}