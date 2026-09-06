package menu.security;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import menu.domain.exception.MenuOpcionNoEncontradaException;

@Provider
public class MenuOpcionNoEncontradaExceptionMapper implements ExceptionMapper<MenuOpcionNoEncontradaException> {

  @Override
  public Response toResponse(MenuOpcionNoEncontradaException exception) {
    return Response.status(Response.Status.NOT_FOUND)
            .entity(new ErrorBody("OPCION_MENU_NO_ENCONTRADA", exception.getMessage()))
            .type(MediaType.APPLICATION_JSON)
            .build();
  }
}