package menu.domain.exception;

public class MenuOpcionNoEncontradaException extends RuntimeException {

  public MenuOpcionNoEncontradaException(Long id) {
    super("No se encontró la opción de menú con id " + id);
  }
}