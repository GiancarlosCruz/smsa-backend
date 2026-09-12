package menu.api;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import menu.api.generated.MenuAdminApi;
import menu.api.generated.model.ActualizarOpcionMenuRequest;
import menu.api.generated.model.AsignarOpcionesCargoRequest;
import menu.api.generated.model.CrearOpcionMenuRequest;
import menu.api.generated.model.OpcionMenuResponse;
import menu.api.generated.model.Rol;
import menu.application.OpcionMenuService;
import menu.domain.model.OpcionMenu;

import java.util.List;

@RequestScoped
@RolesAllowed("ADMIN")
public class MenuAdminResource implements MenuAdminApi {

  @Inject OpcionMenuService opcionMenuService;

  @Override
  public List<OpcionMenuResponse> listarOpciones() {
    return opcionMenuService.listar().stream().map(this::mapear).toList();
  }

  @Override
  public OpcionMenuResponse crearOpcion(CrearOpcionMenuRequest request) {
    return mapear(opcionMenuService.crear(request));
  }

  @Override
  public OpcionMenuResponse actualizarOpcion(Long id, ActualizarOpcionMenuRequest request) {
    return mapear(opcionMenuService.actualizar(id, request));
  }

  @Override
  public void eliminarOpcion(Long id) {
    opcionMenuService.eliminar(id);
  }

  @Override
  public List<OpcionMenuResponse> obtenerOpcionesDeCargo(Long cargoId) {
    return opcionMenuService.obtenerOpcionesDeCargo(cargoId).stream().map(this::mapear).toList();
  }

  @Override
  public void asignarOpcionesACargo(Long cargoId, AsignarOpcionesCargoRequest request) {
    opcionMenuService.asignarOpcionesACargo(cargoId, request.getOpcionesIds());
  }

  @Override
  public List<OpcionMenuResponse> obtenerOpcionesDeRol(Rol rol) {
    return opcionMenuService.obtenerOpcionesDeRol(rol.name()).stream().map(this::mapear).toList();
  }

  @Override
  public void asignarOpcionesARol(Rol rol, AsignarOpcionesCargoRequest request) {
    opcionMenuService.asignarOpcionesARol(rol.name(), request.getOpcionesIds());
  }

  private OpcionMenuResponse mapear(OpcionMenu opcion) {
    OpcionMenuResponse dto = new OpcionMenuResponse();
    dto.setId(opcion.id);
    dto.setCodigo(opcion.codigo);
    dto.setEtiqueta(opcion.etiqueta);
    dto.setIcono(opcion.icono);
    dto.setRuta(opcion.rutaFrontend);
    dto.setOpcionPadreId(opcion.opcionPadre != null ? opcion.opcionPadre : null);
    return dto;
  }
}