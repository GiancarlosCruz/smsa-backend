package users.security;

import users.domain.model.Rol;

import java.util.List;
import java.util.Objects;

/**
 * Contexto de autorización resuelto una vez por request. NO se llena desde
 * claims del token (salvo el sub, usado solo para identificar al usuario):
 * sedesPermitidas siempre se resuelve contra UsuarioSedeRol vía
 * ContextoAccesoProvider, para no depender de un token que puede estar
 * desactualizado frente a un cambio de asignación reciente.
 */
public class ContextoAcceso {

  private final Long usuarioId;
  private final Rol rol;
  private final List<Long> sedesPermitidas; // null = acceso a todas las sedes
  private final boolean adminGlobal;
  private final Long cargoId; // null si el rol no es ADMINISTRATIVO o no tiene cargo asignado

  public ContextoAcceso(Long usuarioId, Rol rol, List<Long> sedesPermitidas, boolean adminGlobal, Long cargoId) {
    this.usuarioId = usuarioId;
    this.rol = rol;
    this.sedesPermitidas = sedesPermitidas;
    this.adminGlobal = adminGlobal;
    this.cargoId = cargoId;
  }

  public Long cargoId() {
    return cargoId;
  }

  public Long usuarioId() {
    return usuarioId;
  }

  public Rol rol() {
    return rol;
  }

  public List<Long> sedesPermitidas() {
    return sedesPermitidas;
  }

  public boolean esAdminGlobal() {
    return adminGlobal;
  }

  /**
   * Para uso de ADMIN sobre un recurso puntual por sede. Docente/estudiante
   * no deberían usar este método: su acceso se valida por dueño del recurso
   * (esElMismoUsuario), no por sede.
   */
  public boolean tieneAccesoASede(Long sedeId) {
    if (adminGlobal) {
      return true;
    }
    return sedesPermitidas != null && sedesPermitidas.contains(sedeId);
  }

  public boolean esElMismoUsuario(Long otroUsuarioId) {
    return Objects.equals(usuarioId, otroUsuarioId);
  }
}