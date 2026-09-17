package menu.application;

import menu.api.generated.model.OpcionMenuResponse;
import users.security.ContextoAcceso;

import java.util.List;

public interface MenuService {

 List<OpcionMenuResponse> obtenerMenuPropio(ContextoAcceso contexto);
}