package users.security;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import users.domain.exception.AccesoSedeNoPermitidoException;

@Provider
public class AccesoSedeExceptionMapper implements ExceptionMapper<AccesoSedeNoPermitidoException> {

  @Override
  public Response toResponse(AccesoSedeNoPermitidoException exception) {
    return Response.status(Response.Status.FORBIDDEN)
            .entity(new ErrorBody("SEDE_NO_PERMITIDA", exception.getMessage()))
            .type(MediaType.APPLICATION_JSON)
            .build();
  }
}