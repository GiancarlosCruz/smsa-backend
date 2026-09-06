package users.security;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import users.domain.exception.CargoNombreDuplicadoException;

@Provider
public class CargoNombreDuplicadoExceptionMapper implements ExceptionMapper<CargoNombreDuplicadoException> {

  @Override
  public Response toResponse(CargoNombreDuplicadoException exception) {
    return Response.status(Response.Status.CONFLICT)
            .entity(new ErrorBody("CARGO_NOMBRE_DUPLICADO", exception.getMessage()))
            .type(MediaType.APPLICATION_JSON)
            .build();
  }
}