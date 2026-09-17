package menu.application;

import menu.api.generated.model.ActualizarOpcionMenuRequest;
import menu.api.generated.model.CrearOpcionMenuRequest;
import menu.domain.model.OpcionMenu;

import java.util.List;

public interface OpcionMenuService {

  List<OpcionMenu> listar();

  OpcionMenu crear(CrearOpcionMenuRequest request);

  OpcionMenu actualizar(Long id, ActualizarOpcionMenuRequest request);

  void eliminar(Long id);

  List<OpcionMenu> obtenerOpcionesDeCargo(Long cargoId);

  List<OpcionMenu> obtenerOpcionesDeRol(String rol);

  void asignarOpcionesARol(String rol, List<Long> opcionesIds);

  void asignarOpcionesACargo(Long cargoId, List<Long> opcionesIds);
}