package users.security;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import users.domain.exception.CargoEnUsoException;

@Provider
public class CargoEnUsoExceptionMapper implements ExceptionMapper<CargoEnUsoException> {

  @Override
  public Response toResponse(CargoEnUsoException exception) {
    return Response.status(Response.Status.CONFLICT)
            .entity(new ErrorBody("CARGO_EN_USO", exception.getMessage()))
            .type(MediaType.APPLICATION_JSON)
            .build();
  }
}