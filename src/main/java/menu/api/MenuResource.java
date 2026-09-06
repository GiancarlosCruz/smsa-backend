package menu.api;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import menu.api.generated.MenuApi;
import menu.api.generated.model.OpcionMenuResponse;
import menu.application.MenuService;
import users.security.ContextoAcceso;

import java.util.List;

@RequestScoped
public class MenuResource implements MenuApi {

  @Inject MenuService menuService;
  @Inject ContextoAcceso contextoAcceso;

  @Override
  @RolesAllowed({"ADMIN", "DOCENTE", "ESTUDIANTE", "ADMINISTRATIVO"})
  public List<OpcionMenuResponse> obtenerMenuPropio() {
    return menuService.obtenerMenuPropio(contextoAcceso);
  }
}