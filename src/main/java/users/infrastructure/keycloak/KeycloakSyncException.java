package users.infrastructure.keycloak;

/**
 * Error en cualquier operación contra el Admin REST API de Keycloak.
 * Se deja como RuntimeException no controlada a propósito: quien la lanza
 * (UsuarioService) es quien debe decidir la compensación explícitamente,
 * no asumir que siempre es recuperable con un simple catch.
 */
public class KeycloakSyncException extends RuntimeException {

  public KeycloakSyncException(String mensaje) {
    super(mensaje);
  }

  public KeycloakSyncException(String mensaje, Throwable causa) {
    super(mensaje, causa);
  }
}