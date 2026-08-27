package users.security;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import users.domain.exception.DocumentoDuplicadoException;

@Provider
public class DocumentoDuplicadoExceptionMapper implements ExceptionMapper<DocumentoDuplicadoException> {

  @Override
  public Response toResponse(DocumentoDuplicadoException exception) {
    return Response.status(Response.Status.CONFLICT)
            .entity(new ErrorBody("DOCUMENTO_DUPLICADO", exception.getMessage()))
            .type(MediaType.APPLICATION_JSON)
            .build();
  }
}