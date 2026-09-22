package estudiantes.domain.mapper;

import estudiantes.domain.exception.EstudianteNoEncontradoException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import menu.security.ErrorBody;

@Provider
public class EstudianteNoEncontradoExceptionMapper implements ExceptionMapper<EstudianteNoEncontradoException> {

  @Override
  public Response toResponse(estudiantes.domain.exception.EstudianteNoEncontradoException exception) {
    return Response.status(Response.Status.NOT_FOUND)
            .entity(new ErrorBody("Estudiante no encontrado", exception.getMessage()))
            .type(MediaType.APPLICATION_JSON)
            .build();
  }
}
