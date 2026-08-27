package users.domain.exception;

public class UsuarioNoEncontradoException extends RuntimeException {

  public UsuarioNoEncontradoException(Long usuarioId) {
    super("No se encontró el usuario con id " + usuarioId);
  }

  public static UsuarioNoEncontradoException porKeycloakId(String keycloakId) {
    return new UsuarioNoEncontradoException(
            "No se encontró un usuario local vinculado al keycloakId " + keycloakId);
  }

  private UsuarioNoEncontradoException(String mensaje) {
    super(mensaje);
  }
}