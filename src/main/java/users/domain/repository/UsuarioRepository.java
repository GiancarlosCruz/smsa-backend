package users.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import users.domain.model.EstadoUsuario;
import users.domain.model.Rol;
import users.domain.model.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UsuarioRepository implements PanacheRepository<Usuario> {

  public Optional<Usuario> buscarPorKeycloakId(UUID keycloakId) {
    return find("keycloakId", keycloakId).firstResultOptional();
  }

  public Optional<Usuario> buscarPorDocumento(String tipoDocumento, String numeroDocumento) {
    return find("tipoDocumento = ?1 and numeroDocumento = ?2", tipoDocumento, numeroDocumento)
            .firstResultOptional();
  }

  public boolean existeDocumento(String tipoDocumento, String numeroDocumento) {
    return count("tipoDocumento = ?1 and numeroDocumento = ?2", tipoDocumento, numeroDocumento) > 0;
  }

  /**
   * Listado para gestión admin, ya restringido a las sedes permitidas del admin autenticado.
   *
   * sedesPermitidas == null  -> admin global, no se filtra por sede.
   * sedesPermitidas vacío    -> el admin no tiene ninguna sede vigente asignada; no debería
   *                             llegar hasta acá (validar antes en el servicio), pero por
   *                             seguridad no se devuelve nada en vez de listar todo.
   */
  public List<Usuario> listarParaAdmin(List<Long> sedesPermitidas, Rol rol, EstadoUsuario estado,
                                       String textoBusqueda, Page page) {
    if (sedesPermitidas != null && sedesPermitidas.isEmpty()) {
      return List.of();
    }

    StringBuilder jpql = new StringBuilder(
            "select distinct u from Usuario u join UsuarioSedeRol usr on usr.usuario = u " +
                    "where (usr.fechaFin is null or usr.fechaFin >= current_date)");
    Parameters params = new Parameters();

    if (sedesPermitidas != null) {
      jpql.append(" and usr.sedeId in (:sedes)");
      params = params.and("sedes", sedesPermitidas);
    }
    if (rol != null) {
      jpql.append(" and usr.rol = :rol");
      params = params.and("rol", rol);
    }
    if (estado != null) {
      jpql.append(" and u.estado = :estado");
      params = params.and("estado", estado);
    }
    if (textoBusqueda != null && !textoBusqueda.isBlank()) {
      jpql.append(" and (lower(u.nombres) like :texto or lower(u.apellidoPaterno) like :texto " +
              "or u.numeroDocumento like :texto)");
      params = params.and("texto", "%" + textoBusqueda.toLowerCase() + "%");
    }

    return find(jpql.toString(), params).page(page).list();
  }
}