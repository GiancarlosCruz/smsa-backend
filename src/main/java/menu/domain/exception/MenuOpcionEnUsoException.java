package menu.domain.exception;

public class MenuOpcionEnUsoException extends RuntimeException {

  public MenuOpcionEnUsoException(Long id) {
    super("La opción de menú " + id + " tiene opciones hijas apuntando a ella, no se puede eliminar");
  }
}