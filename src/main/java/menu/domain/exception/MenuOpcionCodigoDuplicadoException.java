package menu.domain.exception;

public class MenuOpcionCodigoDuplicadoException extends RuntimeException {

  public MenuOpcionCodigoDuplicadoException(String codigo) {
    super("Ya existe una opción de menú con el código '" + codigo + "'");
  }
}