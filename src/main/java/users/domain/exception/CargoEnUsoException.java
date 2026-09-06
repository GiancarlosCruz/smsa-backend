package users.domain.exception;

public class CargoEnUsoException extends RuntimeException {

  public CargoEnUsoException(Long cargoId) {
    super("El cargo " + cargoId + " está en uso por al menos un usuario, no se puede eliminar");
  }
}