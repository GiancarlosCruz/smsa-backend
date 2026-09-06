package users.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import users.api.generated.model.AsignarRolRequest;
import users.domain.exception.AccesoSedeNoPermitidoException;
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
import users.security.ContextoAcceso;

@ApplicationScoped
public class RolSedeService {

  @Inject UsuarioRepository usuarioRepository;
  @Inject UsuarioSedeRolRepository usuarioSedeRolRepository;
  @Inject DocentePerfilRepository docentePerfilRepository;
  @Inject EstudiantePerfilRepository estudiantePerfilRepository;
  @Inject AdministrativoPerfilRepository administrativoPerfilRepository;
  @Inject CargoRepository cargoRepository;

  @Transactional
  public UsuarioSedeRol asignar(Long usuarioId, AsignarRolRequest request, ContextoAcceso contexto) {
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

    UsuarioSedeRol asignacion = new UsuarioSedeRol();
    asignacion.usuario = usuario;
    asignacion.sedeId = request.getSedeId();
    asignacion.rol = rol;
    asignacion.fechaInicio = request.getFechaInicio() != null
            ? request.getFechaInicio()
            : java.time.LocalDate.now();
    asignacion.fechaFin = request.getFechaFin();
    usuarioSedeRolRepository.persist(asignacion);

    // Un mismo usuario puede terminar con roles distintos en momentos
    // distintos (ej. un docente al que después se le asigna también
    // ADMINISTRATIVO). Cada tipo de perfil se crea UNA sola vez por
    // usuario — si ya existe (porque ya tenía ese rol antes, o porque
    // se le está agregando una segunda asignación del mismo rol en otra
    // sede), no se toca ni se duplica.
    crearPerfilSiNoExiste(usuario, rol, request);

    return asignacion;
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
}