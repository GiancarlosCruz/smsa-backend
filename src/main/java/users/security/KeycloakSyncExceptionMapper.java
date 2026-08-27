package users.security;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import users.infrastructure.keycloak.KeycloakSyncException;

/**
 * No se expone el detalle de la excepción al cliente (puede incluir datos de
 * la respuesta cruda del Admin REST API de Keycloak); el detalle completo
 * queda en logs para diagnóstico.
 */
@Provider
public class KeycloakSyncExceptionMapper implements ExceptionMapper<KeycloakSyncException> {

  private static final Logger LOG = Logger.getLogger(KeycloakSyncExceptionMapper.class);

  @Override
  public Response toResponse(KeycloakSyncException exception) {
    LOG.error("Fallo de sincronización con Keycloak", exception);
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(new ErrorBody("ERROR_SINCRONIZACION_KEYCLOAK",
                    "Ocurrió un error al comunicarse con el proveedor de identidad. Intenta nuevamente."))
            .type(MediaType.APPLICATION_JSON)
            .build();
  }
}