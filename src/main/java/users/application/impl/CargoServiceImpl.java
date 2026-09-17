package users.application.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hibernate.exception.ConstraintViolationException;
import users.api.generated.model.ActualizarCargoRequest;
import users.api.generated.model.CrearCargoRequest;
import users.application.CargoService;
import users.domain.exception.CargoEnUsoException;
import users.domain.exception.CargoNoEncontradoException;
import users.domain.exception.CargoNombreDuplicadoException;
import users.domain.model.Cargo;
import users.domain.repository.CargoRepository;

import java.util.List;

@ApplicationScoped
public class CargoServiceImpl implements CargoService {

  private final CargoRepository cargoRepository;

  @Inject
  public CargoServiceImpl(CargoRepository cargoRepository) {
    this.cargoRepository = cargoRepository;
  }

  @Override
  public List<Cargo> listar() {
    return cargoRepository.listAll();
  }

  @Override
  @Transactional
  public Cargo crear(CrearCargoRequest request) {
    if (cargoRepository.existeNombre(request.getNombre())) {
      throw new CargoNombreDuplicadoException(request.getNombre());
    }
    Cargo cargo = new Cargo();
    cargo.nombre = request.getNombre();
    cargo.descripcion = request.getDescripcion();
    cargoRepository.persist(cargo);
    return cargo;
  }

  @Override
  @Transactional
  public Cargo actualizar(Long id, ActualizarCargoRequest request) {
    Cargo cargo = cargoRepository.findByIdOptional(id)
        .orElseThrow(() -> new CargoNoEncontradoException(id));

    if (request.getNombre() != null && !request.getNombre().equals(cargo.nombre)
        && cargoRepository.existeNombre(request.getNombre())) {
      throw new CargoNombreDuplicadoException(request.getNombre());
    }
    if (request.getNombre() != null) {
      cargo.nombre = request.getNombre();
    }
    if (request.getDescripcion() != null) {
      cargo.descripcion = request.getDescripcion();
    }
    return cargo;
  }

  /**
   * No hay chequeo previo "¿está en uso?" — se deja que la FK de
   * administrativo_perfil.cargo_id lo rechace, y se traduce esa violación
   * a un error de negocio claro. Evita una consulta extra en el camino
   * feliz (que es el caso común: cargos que sí se pueden borrar).
   */
  @Override
  @Transactional
  public void eliminar(Long id) {
    Cargo cargo = cargoRepository.findByIdOptional(id)
        .orElseThrow(() -> new CargoNoEncontradoException(id));
    try {
      cargoRepository.delete(cargo);
      cargoRepository.flush();
    } catch (ConstraintViolationException e) {
      throw new CargoEnUsoException(id);
    }
  }
}
