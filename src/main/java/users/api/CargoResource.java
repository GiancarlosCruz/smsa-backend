package users.api;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import users.api.generated.GestionCargosApi;
import users.api.generated.model.ActualizarCargoRequest;
import users.api.generated.model.CargoResponse;
import users.api.generated.model.CrearCargoRequest;
import users.application.CargoService;
import users.domain.model.Cargo;

import java.util.List;

@RequestScoped
@RolesAllowed("ADMIN")
public class CargoResource implements GestionCargosApi {

  @Inject CargoService cargoService;

  @Override
  public List<CargoResponse> listarCargos() {
    return cargoService.listar().stream().map(this::mapear).toList();
  }

  @Override
  public CargoResponse crearCargo(CrearCargoRequest request) {
    return mapear(cargoService.crear(request));
  }

  @Override
  public CargoResponse actualizarCargo(Long id, ActualizarCargoRequest request) {
    return mapear(cargoService.actualizar(id, request));
  }

  @Override
  public void eliminarCargo(Long id) {
    cargoService.eliminar(id);
  }

  private CargoResponse mapear(Cargo cargo) {
    CargoResponse dto = new CargoResponse();
    dto.setId(cargo.id);
    dto.setNombre(cargo.nombre);
    dto.setDescripcion(cargo.descripcion);
    return dto;
  }
}