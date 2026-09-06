package users.domain.exception;

public class CargoNoEncontradoException extends RuntimeException {

  public CargoNoEncontradoException(Long cargoId) {
    super("No se encontró el cargo con id " + cargoId);
  }
}