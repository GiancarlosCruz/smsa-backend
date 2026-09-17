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

  private final MenuService menuService;
  private final ContextoAcceso contextoAcceso;

  @Inject
  public MenuResource (MenuService menuService, ContextoAcceso contextoAcceso){
    this.menuService = menuService;
    this.contextoAcceso = contextoAcceso;
  }

  @Override
  @RolesAllowed({"ADMIN", "DOCENTE", "ESTUDIANTE", "ADMINISTRATIVO"})
  public List<OpcionMenuResponse> obtenerMenuPropio() {
    return menuService.obtenerMenuPropio(contextoAcceso);
  }
}