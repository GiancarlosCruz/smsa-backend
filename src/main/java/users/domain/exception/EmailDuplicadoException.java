package users.domain.exception;

public class EmailDuplicadoException extends RuntimeException {

  public EmailDuplicadoException(String email) {
    super("El email ya está registrado: " + email);
  }

}
