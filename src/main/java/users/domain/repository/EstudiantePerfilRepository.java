package users.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import users.domain.model.EstudiantePerfil;

import java.util.Optional;

@ApplicationScoped
public class EstudiantePerfilRepository implements PanacheRepositoryBase<EstudiantePerfil, Long> {

  public Optional<EstudiantePerfil> buscarPorUsuarioId(Long usuarioId) {
    return findByIdOptional(usuarioId);
  }

  public Optional<EstudiantePerfil> buscarPorCodigoEstudiante(String codigoEstudiante) {
    return find("codigoEstudiante", codigoEstudiante).firstResultOptional();
  }

  public boolean existeCodigoEstudiante(String codigoEstudiante) {
    return count("codigoEstudiante", codigoEstudiante) > 0;
  }
}