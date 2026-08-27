package users.security;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import users.domain.exception.UsuarioNoEncontradoException;

@Provider
public class UsuarioNoEncontradoExceptionMapper implements ExceptionMapper<UsuarioNoEncontradoException> {

  @Override
  public Response toResponse(UsuarioNoEncontradoException exception) {
    return Response.status(Response.Status.NOT_FOUND)
            .entity(new ErrorBody("USUARIO_NO_ENCONTRADO", exception.getMessage()))
            .type(MediaType.APPLICATION_JSON)
            .build();
  }
}