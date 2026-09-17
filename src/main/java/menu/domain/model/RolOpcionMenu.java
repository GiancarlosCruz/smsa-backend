package menu.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rol_opcion_menu")
public class RolOpcionMenu {

  @EmbeddedId
  private RolOpcionMenuId id = new RolOpcionMenuId();

  @MapsId("opcionMenuId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "opcion_menu_id", nullable = false)
  private OpcionMenu opcionMenu;

  public RolOpcionMenu() {
  }

  public RolOpcionMenu(users.domain.model.Rol rol, OpcionMenu opcionMenu) {
    this.opcionMenu = opcionMenu;
    this.id = new RolOpcionMenuId(rol, opcionMenu != null ? opcionMenu.id : null);
  }

  public RolOpcionMenuId getId() {
    return id;
  }

  public void setId(RolOpcionMenuId id) {
    this.id = id;
  }

  public OpcionMenu getOpcionMenu() {
    return opcionMenu;
  }

  public void setOpcionMenu(OpcionMenu opcionMenu) {
    this.opcionMenu = opcionMenu;
    if (this.id == null) {
      this.id = new RolOpcionMenuId();
    }
    this.id.setOpcionMenuId(opcionMenu != null ? opcionMenu.id : null);
  }
}
