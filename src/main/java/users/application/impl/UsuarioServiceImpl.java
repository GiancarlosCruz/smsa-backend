package users.application.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import users.api.generated.model.*;
import users.application.UsuarioService;
import users.config.UserSeedProperties;
import users.domain.exception.AccesoSedeNoPermitidoException;
import users.domain.exception.DocumentoDuplicadoException;
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
import java.time.ZoneOffset;
import java.util.UUID;

@ApplicationScoped
public class UsuarioServiceImpl implements UsuarioService {

  private static final Logger LOG = Logger.getLogger(UsuarioServiceImpl.class);

  private final UsuarioRepository usuarioRepository;
  private final UsuarioSedeRolRepository usuarioSedeRolRepository;
  private final DocentePerfilRepository docentePerfilRepository;
  private final EstudiantePerfilRepository estudiantePerfilRepository;
  private final AdministrativoPerfilRepository administrativoPerfilRepository;
  private final CargoRepository cargoRepository;
  private final KeycloakProvisioningService keycloakProvisioningService;
  private final UserSeedProperties userSeedProperties;

  @Inject
  public UsuarioServiceImpl(UsuarioRepository usuarioRepository, UsuarioSedeRolRepository usuarioSedeRolRepository, DocentePerfilRepository docentePerfilRepository, EstudiantePerfilRepository estudiantePerfilRepository, AdministrativoPerfilRepository administrativoPerfilRepository, CargoRepository cargoRepository, KeycloakProvisioningService keycloakProvisioningService, UserSeedProperties userSeedProperties) {
    this.usuarioRepository = usuarioRepository;
    this.usuarioSedeRolRepository = usuarioSedeRolRepository;
    this.docentePerfilRepository = docentePerfilRepository;
    this.estudiantePerfilRepository = estudiantePerfilRepository;
    this.administrativoPerfilRepository = administrativoPerfilRepository;
    this.cargoRepository = cargoRepository;
    this.keycloakProvisioningService = keycloakProvisioningService;
    this.userSeedProperties = userSeedProperties;
  }

  /**
   * Keycloak primero (fuera de la transacción local, es una llamada de red
   * a un sistema externo), luego la escritura local ya transaccional.
   * Si la escritura local falla, se compensa eliminando lo creado en Keycloak.
   */
  @Override
  public Usuario crear(CrearUsuarioRequest request, ContextoAcceso contexto) {
    if (usuarioRepository.existeDocumento(request.getTipoDocumento(), request.getNumeroDocumento())) {
      throw new DocumentoDuplicadoException(request.getTipoDocumento(), request.getNumeroDocumento());
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
    asignacion.sedeId = request.getSedeId();
    asignacion.rol = rol;
    asignacion.fechaInicio = LocalDate.now(ZoneOffset.UTC);
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

  /**
   * Actualiza datos base (todos los campos de ActualizarUsuarioRequest, no
   * solo los 3 que cubría antes) y, si vienen, los datos de cada perfil —
   * pero solo si ese perfil YA EXISTE para este usuario. No crea un perfil
   * nuevo acá: para agregar un rol/perfil que el usuario no tenía, se usa
   * POST /usuarios/{id}/roles (que sí crea la fila correspondiente).
   */
  @Override
  @Transactional
  public Usuario actualizar(Long usuarioId, ActualizarUsuarioRequest request, ContextoAcceso contexto) {
    Usuario usuario = usuarioRepository.findByIdOptional(usuarioId)
            .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));

    validarAccesoSobreUsuario(usuario, contexto);

    actualizarDatosUsuario(usuario, request);
    actualizarPerfilDocente(usuario.id, request.getPerfilDocente());
    actualizarPerfilEstudiante(usuario.id, request.getPerfilEstudiante());
    actualizarPerfilAdministrativo(usuario.id, request.getPerfilAdministrativo());

    keycloakProvisioningService.actualizarDatosBasicos(
            usuario.keycloakId, usuario.nombres, usuario.apellidoPaterno, usuario.email);

    return usuario;
  }

  private void actualizarDatosUsuario(Usuario usuario, ActualizarUsuarioRequest request) {
    if (request.getNombres() != null) usuario.nombres = request.getNombres();
    if (request.getApellidoPaterno() != null) usuario.apellidoPaterno = request.getApellidoPaterno();
    if (request.getApellidoMaterno() != null) usuario.apellidoMaterno = request.getApellidoMaterno();
    if (request.getTelefono() != null) usuario.telefono = request.getTelefono();
    if (request.getSexo() != null) {
      usuario.sexo = users.domain.model.Sexo.valueOf(request.getSexo().name()).name();
    }
    if (request.getFechaNacimiento() != null) usuario.fechaNacimiento = request.getFechaNacimiento();
  }

  private void actualizarPerfilDocente(Long usuarioId, PerfilDocente dto) {
    if (dto == null) return;

    docentePerfilRepository.buscarPorUsuarioId(usuarioId).ifPresent(perfil -> {
      if (dto.getGradoAcademico() != null) perfil.gradoAcademico = dto.getGradoAcademico();
      if (dto.getEspecialidad() != null) perfil.especialidad = dto.getEspecialidad();
      if (dto.getTipoContrato() != null) perfil.tipoContrato = dto.getTipoContrato();
      if (dto.getFechaIngreso() != null) perfil.fechaIngreso = dto.getFechaIngreso();
      if (dto.getDireccion() != null) perfil.direccion = dto.getDireccion();
      if (dto.getCargoAdministrativo() != null) perfil.cargoAdministrativo = dto.getCargoAdministrativo();
    });
  }

  private void actualizarPerfilEstudiante(Long usuarioId, PerfilEstudiante dto) {
    if (dto == null) return;

    estudiantePerfilRepository.buscarPorUsuarioId(usuarioId).ifPresent(perfil -> {
      if (dto.getProgramaId() != null) perfil.programaId = dto.getProgramaId();
      if (dto.getContactoEmergenciaNombre() != null) {
        perfil.contactoEmergenciaNombre = dto.getContactoEmergenciaNombre();
      }
      if (dto.getContactoEmergenciaTelefono() != null) {
        perfil.contactoEmergenciaTelefono = dto.getContactoEmergenciaTelefono();
      }
      if (dto.getDireccion() != null) perfil.direccion = dto.getDireccion();
    });
  }

  private void actualizarPerfilAdministrativo(Long usuarioId, PerfilAdministrativo dto) {
    if (dto == null) return;

    administrativoPerfilRepository.buscarPorUsuarioId(usuarioId).ifPresent(perfil -> {
      if (dto.getCargoId() != null) {
        perfil.cargo = cargoRepository.findByIdOptional(dto.getCargoId())
                .orElseThrow(() -> new IllegalArgumentException("cargoId inexistente: " + dto.getCargoId()));
      }
      if (dto.getAreaAdministrativa() != null) perfil.areaAdministrativa = dto.getAreaAdministrativa();
      if (dto.getCondicion() != null) perfil.condicion = dto.getCondicion();
    });
  }

  @Override
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

  /**
   * Bootstrap: si no existe ningún usuario con rol ADMIN, crea uno con los
   * datos de seed.admin.* (env vars en prod). Resuelve el bootstrap
   * paradox: POST /usuarios y POST /usuarios/{id}/roles ya exigen ser
   * ADMIN, así que el primer ADMIN no se puede crear por la API — se
   * siembra al arrancar la app, sin ContextoAcceso porque no hay sesión
   * todavía. Idempotente: no hace nada en arranques posteriores una vez
   * que ya existe un ADMIN.
   */
  @Override
  public void sembrarAdminSiNoExiste() {
    if (!userSeedProperties.isSeedAdminEnabled()) {
      return;
    }
    if (usuarioSedeRolRepository.existeAlgunAdmin()) {
      return;
    }
    if (usuarioRepository.existeDocumento(userSeedProperties.getSeedAdminTipoDocumento(), userSeedProperties.getSeedAdminNumeroDocumento())) {
      LOG.warnf("seed.admin.numero-documento (%s) ya existe como usuario pero sin rol ADMIN vigente — " +
              "revisa manualmente, no se sembró un admin nuevo.", userSeedProperties.getSeedAdminNumeroDocumento());
      return;
    }

    LOG.info("No existe ningún ADMIN todavía, sembrando el admin inicial desde configuración (seed.admin.*)");

    UUID keycloakId = keycloakProvisioningService.crearUsuario(
            userSeedProperties.getSeedAdminNumeroDocumento(), userSeedProperties.getSeedAdminEmail(), userSeedProperties.getSeedAdminNombres(), userSeedProperties.getSeedAdminApellidoPaterno(), Rol.ADMIN);

    try {
      persistirAdminBootstrap(keycloakId);
      LOG.infof("Admin inicial creado: username/password temporal = %s", userSeedProperties.getSeedAdminNumeroDocumento());
    } catch (RuntimeException e) {
      LOG.errorf(e, "Falló la escritura local del admin inicial (keycloakId=%s), compensando", keycloakId);
      keycloakProvisioningService.eliminarPorId(keycloakId);
      throw e;
    }
  }

  @Transactional
  protected void persistirAdminBootstrap(UUID keycloakId) {
    Usuario usuario = new Usuario();
    usuario.keycloakId = keycloakId;
    usuario.tipoDocumento = userSeedProperties.getSeedAdminTipoDocumento();
    usuario.numeroDocumento = userSeedProperties.getSeedAdminNumeroDocumento();
    usuario.nombres = userSeedProperties.getSeedAdminNombres();
    usuario.apellidoPaterno = userSeedProperties.getSeedAdminApellidoPaterno();
    usuario.email = userSeedProperties.getSeedAdminEmail();
    usuario.estado = EstadoUsuario.ACTIVO;
    usuario.fechaNacimiento = userSeedProperties.getSeedAdminFechaNacimiento();
    usuarioRepository.persist(usuario);

    UsuarioSedeRol asignacion = new UsuarioSedeRol();
    asignacion.usuario = usuario;
    asignacion.sedeId = null;
    asignacion.rol = Rol.ADMIN;
    asignacion.fechaInicio = LocalDate.now(ZoneOffset.UTC);
    usuarioSedeRolRepository.persist(asignacion);
  }
}
