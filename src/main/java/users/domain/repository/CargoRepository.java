package users.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import users.domain.model.Cargo;

@ApplicationScoped
public class CargoRepository implements PanacheRepositoryBase<Cargo, Long> {

  public boolean existeNombre(String nombre) {
    return count("nombre", nombre) > 0;
  }
}