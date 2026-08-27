package users.security;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import users.domain.exception.UsuarioYaExisteException;

import java.util.Map;

@Provider
public class UsuarioYaExisteExceptionMapper implements ExceptionMapper<UsuarioYaExisteException> {

  @Override
  public Response toResponse(UsuarioYaExisteException exception) {
    Map<String, String> errorResponse = Map.of(
            "codigo", "USUARIO_DUPLICADO",
            "mensaje", exception.getMessage()
    );

    return Response.status(Response.Status.CONFLICT)
            .type(MediaType.APPLICATION_JSON)
            .entity(errorResponse)
            .build();
  }
}