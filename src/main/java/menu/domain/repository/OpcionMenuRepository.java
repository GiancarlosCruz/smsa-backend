package menu.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import menu.domain.model.OpcionMenu;

import java.util.List;

@ApplicationScoped
public class OpcionMenuRepository implements PanacheRepositoryBase<OpcionMenu, Long> {

  public boolean existeCodigo(String codigo) {
    return count("codigo", codigo) > 0;
  }

  /**
   * cargo_opcion_menu es una tabla puente simple (cargo_id, opcion_menu_id)
   * sin atributos propios. No se modela como entidad de clave compuesta —
   * se resuelve con queries nativas directas. cargoId llega por valor
   * desde el módulo usuarios (vía ContextoAcceso o el path param del
   * endpoint de asignación), sin relación JPA entre módulos.
   */
  public List<OpcionMenu> buscarPorCargoId(Long cargoId) {
    return getEntityManager()
            .createNativeQuery(
                    "select om.* from opcion_menu om " +
                            "join cargo_opcion_menu com on com.opcion_menu_id = om.id " +
                            "where com.cargo_id = :cargoId " +
                            "order by om.orden",
                    OpcionMenu.class)
            .setParameter("cargoId", cargoId)
            .getResultList();
  }

  /**
   * Menú base por rol — a diferencia de cargoId (solo aplica a
   * ADMINISTRATIVO), esto aplica a cualquiera de los 4 roles. rol llega
   * como String (el name() del enum), no como tipo — este módulo no
   * depende del enum Rol de usuarios, sigue el mismo criterio que cargoId.
   */
  public List<OpcionMenu> buscarPorRol(String rol) {
    return getEntityManager()
            .createNativeQuery(
                    "select om.* from opcion_menu om " +
                            "join rol_opcion_menu rom on rom.opcion_menu_id = om.id " +
                            "where rom.rol = :rol " +
                            "order by om.orden",
                    OpcionMenu.class)
            .setParameter("rol", rol)
            .getResultList();
  }

  /**
   * Reemplaza el conjunto completo de opciones asignadas a un rol.
   * El llamador (OpcionMenuService) es responsable de la transacción.
   */
  public void reemplazarOpcionesDeRol(String rol, List<Long> opcionesIds) {
    getEntityManager()
            .createNativeQuery("delete from rol_opcion_menu where rol = :rol")
            .setParameter("rol", rol)
            .executeUpdate();

    for (Long opcionId : opcionesIds) {
      getEntityManager()
              .createNativeQuery(
                      "insert into rol_opcion_menu (rol, opcion_menu_id) values (:rol, :opcionId)")
              .setParameter("rol", rol)
              .setParameter("opcionId", opcionId)
              .executeUpdate();
    }
  }

  /**
   * Reemplaza el conjunto completo de opciones asignadas a un cargo.
   * El llamador (OpcionMenuService) es responsable de la transacción.
   */
  public void reemplazarOpcionesDeCargo(Long cargoId, List<Long> opcionesIds) {
    getEntityManager()
            .createNativeQuery("delete from cargo_opcion_menu where cargo_id = :cargoId")
            .setParameter("cargoId", cargoId)
            .executeUpdate();

    for (Long opcionId : opcionesIds) {
      getEntityManager()
              .createNativeQuery(
                      "insert into cargo_opcion_menu (cargo_id, opcion_menu_id) values (:cargoId, :opcionId)")
              .setParameter("cargoId", cargoId)
              .setParameter("opcionId", opcionId)
              .executeUpdate();
    }
  }
}