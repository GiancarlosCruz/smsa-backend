package users.security;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import users.domain.exception.AsignacionRolNoEncontradaException;

@Provider
public class AsignacionRolNoEncontradaExceptionMapper implements ExceptionMapper<AsignacionRolNoEncontradaException> {

  @Override
  public Response toResponse(AsignacionRolNoEncontradaException exception) {
    return Response.status(Response.Status.NOT_FOUND)
            .entity(new ErrorBody("ASIGNACION_ROL_NO_ENCONTRADA", exception.getMessage()))
            .type(MediaType.APPLICATION_JSON)
            .build();
  }
}