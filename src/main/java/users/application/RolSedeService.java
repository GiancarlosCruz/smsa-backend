package users.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import users.api.generated.model.AsignarRolRequest;
import users.domain.exception.AccesoSedeNoPermitidoException;
import users.domain.exception.UsuarioNoEncontradoException;
import users.domain.model.Rol;
import users.domain.model.Usuario;
import users.domain.model.UsuarioSedeRol;
import users.domain.repository.UsuarioRepository;
import users.domain.repository.UsuarioSedeRolRepository;
import users.security.ContextoAcceso;

@ApplicationScoped
public class RolSedeService {

  @Inject UsuarioRepository usuarioRepository;
  @Inject UsuarioSedeRolRepository usuarioSedeRolRepository;

  @Transactional
  public UsuarioSedeRol asignar(Long usuarioId, AsignarRolRequest request, ContextoAcceso contexto) {
    Usuario usuario = usuarioRepository.findByIdOptional(usuarioId)
            .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));

    Rol rol = Rol.valueOf(request.getRol().name());

    // sedeId == null solo es válido para ADMIN (acceso global); para
    // DOCENTE/ESTUDIANTE una sede nula sería un usuario sin ámbito de datos.
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
    return asignacion;
  }
}