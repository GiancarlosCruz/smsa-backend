package users.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import users.api.generated.model.AsignarRolRequest;
import users.domain.exception.AccesoSedeNoPermitidoException;
import users.domain.exception.AsignacionRolNoEncontradaException;
import users.domain.exception.UsuarioNoEncontradoException;
import users.domain.model.AdministrativoPerfil;
import users.domain.model.DocentePerfil;
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

@ApplicationScoped
public class RolSedeService {

  private static final Logger LOG = Logger.getLogger(RolSedeService.class);

  private final UsuarioRepository usuarioRepository;
  private final UsuarioSedeRolRepository usuarioSedeRolRepository;
  private final DocentePerfilRepository docentePerfilRepository;
  private final EstudiantePerfilRepository estudiantePerfilRepository;
  private final AdministrativoPerfilRepository administrativoPerfilRepository;
  private final CargoRepository cargoRepository;
  private final KeycloakProvisioningService keycloakProvisioningService;

  @Inject
  public RolSedeService(UsuarioRepository usuarioRepository,
                        UsuarioSedeRolRepository usuarioSedeRolRepository,
                        DocentePerfilRepository docentePerfilRepository,
                        EstudiantePerfilRepository estudiantePerfilRepository,
                        AdministrativoPerfilRepository administrativoPerfilRepository,
                        CargoRepository cargoRepository,
                        KeycloakProvisioningService keycloakProvisioningService) {
    this.usuarioRepository = usuarioRepository;
    this.usuarioSedeRolRepository = usuarioSedeRolRepository;
    this.docentePerfilRepository = docentePerfilRepository;
    this.estudiantePerfilRepository = estudiantePerfilRepository;
    this.administrativoPerfilRepository = administrativoPerfilRepository;
    this.cargoRepository = cargoRepository;
    this.keycloakProvisioningService = keycloakProvisioningService;
  }

  /**
   * Igual que UsuarioService.crear: Keycloak primero (fuera de la
   * transacción local), luego la escritura local. Si la escritura local
   * falla, se compensa quitando el rol recién agregado en Keycloak.
   */
  public void asignar(Long usuarioId, AsignarRolRequest request, ContextoAcceso contexto) {
    Usuario usuario = usuarioRepository.findByIdOptional(usuarioId)
            .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));

    Rol rol = Rol.valueOf(request.getRol().name());

    // sedeId == null solo es válido para ADMIN (acceso global); para
    // DOCENTE/ESTUDIANTE/ADMINISTRATIVO una sede nula sería un usuario
    // sin ámbito de datos.
    if (request.getSedeId() == null && rol != Rol.ADMIN) {
      throw new IllegalArgumentException(
              "sedeId es obligatorio para el rol " + rol + " (solo ADMIN puede tener acceso global)");
    }

    // Quien asigna debe tener acceso a la sede que está otorgando; un
    // admin de sede no puede otorgar acceso a una sede fuera de la suya,
    // ni otorgar acceso global (eso solo lo hace un admin global).
    if (!contexto.esAdminGlobal()) {
      if (request.getSedeId() == null) {
        throw new AccesoSedeNoPermitidoException(null);
      }
      if (!contexto.tieneAccesoASede(request.getSedeId())) {
        throw new AccesoSedeNoPermitidoException(request.getSedeId());
      }
    }

    keycloakProvisioningService.agregarRol(usuario.keycloakId, rol);

    try {
      persistirAsignacion(usuario.id, rol, request);
    } catch (RuntimeException e) {
      LOG.errorf(e,
              "Falló la escritura local tras agregar el rol %s en Keycloak (keycloakId=%s), compensando",
              rol, usuario.keycloakId);
      keycloakProvisioningService.quitarRol(usuario.keycloakId, rol);
      throw e;
    }
  }

  @Transactional
  protected void persistirAsignacion(Long usuarioId, Rol rol, AsignarRolRequest request) {
    Usuario usuarioPersist = usuarioRepository.findByIdOptional(usuarioId)
            .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));
    UsuarioSedeRol asignacion = new UsuarioSedeRol();
    asignacion.usuario = usuarioPersist;
    asignacion.sedeId = request.getSedeId();
    asignacion.rol = rol;
    asignacion.fechaInicio = request.getFechaInicio() != null
            ? request.getFechaInicio()
            : java.time.LocalDate.now(java.time.ZoneId.of("America/Lima"));
    asignacion.fechaFin = request.getFechaFin();
    usuarioSedeRolRepository.persist(asignacion);

    // Un mismo usuario puede terminar con roles distintos en momentos
    // distintos (ej. un docente al que después se le asigna también
    // ADMINISTRATIVO). Cada tipo de perfil se crea UNA sola vez por
    // usuario — si ya existe (porque ya tenía ese rol antes, o porque
    // se le está agregando una segunda asignación del mismo rol en otra
    // sede), no se toca ni se duplica.
    crearPerfilSiNoExiste(usuarioPersist, rol, request);

  }

  private void crearPerfilSiNoExiste(Usuario usuario, Rol rol, AsignarRolRequest request) {
    if (rol == Rol.DOCENTE && request.getPerfilDocente() != null
            && docentePerfilRepository.buscarPorUsuarioId(usuario.id).isEmpty()) {
      var dto = request.getPerfilDocente();
      DocentePerfil perfil = new DocentePerfil();
      perfil.usuario = usuario;
      perfil.gradoAcademico = dto.getGradoAcademico();
      perfil.especialidad = dto.getEspecialidad();
      perfil.tipoContrato = dto.getTipoContrato();
      perfil.fechaIngreso = dto.getFechaIngreso();
      perfil.direccion = dto.getDireccion();
      perfil.cargoAdministrativo = dto.getCargoAdministrativo();
      docentePerfilRepository.persist(perfil);
    }

    if (rol == Rol.ESTUDIANTE && request.getPerfilEstudiante() != null
            && estudiantePerfilRepository.buscarPorUsuarioId(usuario.id).isEmpty()) {
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

    if (rol == Rol.ADMINISTRATIVO && request.getPerfilAdministrativo() != null
            && administrativoPerfilRepository.buscarPorUsuarioId(usuario.id).isEmpty()) {
      var dto = request.getPerfilAdministrativo();
      AdministrativoPerfil perfil = new AdministrativoPerfil();
      perfil.usuario = usuario;
      perfil.cargo = cargoRepository.findByIdOptional(dto.getCargoId())
              .orElseThrow(() -> new IllegalArgumentException("cargoId inexistente: " + dto.getCargoId()));
      perfil.areaAdministrativa = dto.getAreaAdministrativa();
      perfil.condicion = dto.getCondicion();
      administrativoPerfilRepository.persist(perfil);
    }
  }

  /**
   * Termina la asignación vigente (rol+sede) marcando fechaFin, y solo
   * toca Keycloak si esa era la ÚLTIMA asignación vigente de ese rol para
   * el usuario — si sigue teniendo el mismo rol en otra sede, el rol de
   * Keycloak se mantiene (ahí no hay noción de sede).
   */
  @Transactional
  public void revocar(Long usuarioId, Rol rol, Long sedeId, ContextoAcceso contexto) {
    Usuario usuario = usuarioRepository.findByIdOptional(usuarioId)
            .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));

    if (!contexto.esAdminGlobal() && (sedeId == null || !contexto.tieneAccesoASede(sedeId))) {
      throw new AccesoSedeNoPermitidoException(sedeId);
    }


    var todasVigentes = usuarioSedeRolRepository.vigentesDe(usuario.id);
    var aRevocar = todasVigentes.stream()
            .filter(a -> a.rol == rol && java.util.Objects.equals(a.sedeId, sedeId))
            .toList();

    if (aRevocar.isEmpty()) {
      throw new AsignacionRolNoEncontradaException(usuarioId, rol, sedeId);
    }

    aRevocar.forEach(a -> a.fechaFin = java.time.LocalDate.now(java.time.ZoneId.of("America/Lima")));

    boolean quedanOtrasDelMismoRol = todasVigentes.stream()
            .filter(a -> !aRevocar.contains(a))
            .anyMatch(a -> a.rol == rol);

    if (!quedanOtrasDelMismoRol) {
      keycloakProvisioningService.quitarRol(usuario.keycloakId, rol);
    }
  }
}