package users.domain.exception;

import users.domain.model.Rol;

public class AsignacionRolNoEncontradaException extends RuntimeException {

  public AsignacionRolNoEncontradaException(Long usuarioId, Rol rol, Long sedeId) {
    super("El usuario " + usuarioId + " no tiene una asignación vigente de " + rol
            + (sedeId != null ? " en la sede " + sedeId : " con acceso global"));
  }
}