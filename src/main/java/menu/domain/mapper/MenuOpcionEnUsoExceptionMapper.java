package menu.domain.mapper;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import menu.domain.exception.MenuOpcionEnUsoException;
import menu.security.ErrorBody;

@Provider
public class MenuOpcionEnUsoExceptionMapper implements ExceptionMapper<MenuOpcionEnUsoException> {

  @Override
  public Response toResponse(MenuOpcionEnUsoException exception) {
    return Response.status(Response.Status.CONFLICT)
            .entity(new ErrorBody("OPCION_MENU_EN_USO", exception.getMessage()))
            .type(MediaType.APPLICATION_JSON)
            .build();
  }
}
