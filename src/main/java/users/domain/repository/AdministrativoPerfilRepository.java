package users.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import users.domain.model.AdministrativoPerfil;

import java.util.Optional;

@ApplicationScoped
public class AdministrativoPerfilRepository implements PanacheRepositoryBase<AdministrativoPerfil, Long> {

  public Optional<AdministrativoPerfil> buscarPorUsuarioId(Long usuarioId) {
    return findByIdOptional(usuarioId);
  }
}