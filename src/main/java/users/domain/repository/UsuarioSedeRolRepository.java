package users.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import users.domain.model.Rol;
import users.domain.model.UsuarioSedeRol;

import java.util.List;

@ApplicationScoped
public class UsuarioSedeRolRepository implements PanacheRepository<UsuarioSedeRol> {

  /**
   * Asignaciones vigentes de sede/rol de un usuario. Esta es la consulta que
   * ContextoAccesoProvider ejecuta una vez por request para resolver
   * sedesPermitidas y esAdminGlobal — nunca traer el historial completo.
   */
  public List<UsuarioSedeRol> vigentesDe(Long usuarioId) {
    return list(
            "usuario.id = ?1 and (fechaFin is null or fechaFin >= current_date)",
            usuarioId
    );
  }

  public boolean tieneRolVigente(Long usuarioId, Rol rol) {
    return count(
            "usuario.id = ?1 and rol = ?2 and (fechaFin is null or fechaFin >= current_date)",
            usuarioId, rol
    ) > 0;
  }
}