package users.security;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import users.domain.exception.CargoNoEncontradoException;

@Provider
public class CargoNoEncontradoExceptionMapper implements ExceptionMapper<CargoNoEncontradoException> {

  @Override
  public Response toResponse(CargoNoEncontradoException exception) {
    return Response.status(Response.Status.NOT_FOUND)
            .entity(new ErrorBody("CARGO_NO_ENCONTRADO", exception.getMessage()))
            .type(MediaType.APPLICATION_JSON)
            .build();
  }
}