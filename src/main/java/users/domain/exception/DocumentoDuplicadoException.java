package users.domain.exception;

public class DocumentoDuplicadoException extends RuntimeException {

  public DocumentoDuplicadoException(String tipoDocumento, String numeroDocumento) {
    super("Ya existe un usuario registrado con documento %s %s"
            .formatted(tipoDocumento, numeroDocumento));
  }
}