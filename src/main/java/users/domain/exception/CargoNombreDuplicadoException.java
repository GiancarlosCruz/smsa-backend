package users.domain.exception;

public class CargoNombreDuplicadoException extends RuntimeException {

  public CargoNombreDuplicadoException(String nombre) {
    super("Ya existe un cargo con el nombre '" + nombre + "'");
  }
}