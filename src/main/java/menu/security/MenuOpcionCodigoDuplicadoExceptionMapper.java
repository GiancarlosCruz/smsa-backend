package menu.security;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import menu.domain.exception.MenuOpcionCodigoDuplicadoException;

@Provider
public class MenuOpcionCodigoDuplicadoExceptionMapper implements ExceptionMapper<MenuOpcionCodigoDuplicadoException> {

  @Override
  public Response toResponse(MenuOpcionCodigoDuplicadoException exception) {
    return Response.status(Response.Status.CONFLICT)
            .entity(new ErrorBody("OPCION_MENU_CODIGO_DUPLICADO", exception.getMessage()))
            .type(MediaType.APPLICATION_JSON)
            .build();
  }
}