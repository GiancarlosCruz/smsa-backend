package users.application;

import users.api.generated.model.ActualizarCargoRequest;
import users.api.generated.model.CrearCargoRequest;
import users.domain.model.Cargo;

import java.util.List;

public interface CargoService {

 List<Cargo> listar();

 Cargo crear(CrearCargoRequest request);

 Cargo actualizar(Long id, ActualizarCargoRequest request);

 void eliminar(Long id);
}