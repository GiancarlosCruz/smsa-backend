package estudiantes.domain.repository;

import estudiantes.domain.model.Estudiante;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class EstudianteRepository implements PanacheRepositoryBase<Estudiante, Long> {

  public Optional<Estudiante> findByUsuarioId(Long usuarioId) {
    return find("usuarioId", usuarioId).firstResultOptional();
  }

  public boolean existeCodigoEstudiante(String codigo) {
    return count("codigoEstudiante", codigo) > 0;
  }
}