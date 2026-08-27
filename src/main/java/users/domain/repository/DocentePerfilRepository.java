package users.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import users.domain.model.DocentePerfil;

import java.util.Optional;

/**
 * PanacheRepositoryBase<DocentePerfil, Long> porque DocentePerfil no extiende
 * PanacheEntity (su id es usuarioId, compartido con Usuario vía @MapsId).
 */
@ApplicationScoped
public class DocentePerfilRepository implements PanacheRepositoryBase<DocentePerfil, Long> {

  public Optional<DocentePerfil> buscarPorUsuarioId(Long usuarioId) {
    return findByIdOptional(usuarioId);
  }
}