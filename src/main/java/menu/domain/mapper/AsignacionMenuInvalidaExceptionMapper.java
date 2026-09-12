package menu.domain.mapper;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import menu.domain.exception.AsignacionMenuInvalidaException;
import menu.security.ErrorBody;

@Provider
public class AsignacionMenuInvalidaExceptionMapper implements ExceptionMapper<AsignacionMenuInvalidaException> {

  @Override
  public Response toResponse(AsignacionMenuInvalidaException exception) {
    return Response.status(Response.Status.BAD_REQUEST)
            .entity(new ErrorBody("ASIGNACION_MENU_INVALIDA", exception.getMessage()))
            .type(MediaType.APPLICATION_JSON)
            .build();
  }
}
