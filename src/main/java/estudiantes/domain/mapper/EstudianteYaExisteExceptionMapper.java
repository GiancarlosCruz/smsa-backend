package estudiantes.domain.mapper;

import estudiantes.domain.exception.EstudianteYaExisteException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import menu.security.ErrorBody;

@Provider
public class EstudianteYaExisteExceptionMapper implements ExceptionMapper<EstudianteYaExisteException> {
  @Override
  public Response toResponse(estudiantes.domain.exception.EstudianteYaExisteException exception) {
    return Response.status(Response.Status.CONFLICT)
            .entity(new ErrorBody("Estudiante ya existe", exception.getMessage()))
            .type(MediaType.APPLICATION_JSON)
            .build();
  }
}

