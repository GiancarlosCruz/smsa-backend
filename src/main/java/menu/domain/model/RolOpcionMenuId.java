package menu.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import users.domain.model.Rol;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RolOpcionMenuId implements Serializable {

  @Enumerated(EnumType.STRING)
  @Column(name = "rol", length = 20, nullable = false)
  private Rol rol;

  @Column(name = "opcion_menu_id", nullable = false)
  private Long opcionMenuId;

  public RolOpcionMenuId() {
  }

  public RolOpcionMenuId(Rol rol, Long opcionMenuId) {
    this.rol = rol;
    this.opcionMenuId = opcionMenuId;
  }

  public Rol getRol() {
    return rol;
  }

  public void setRol(Rol rol) {
    this.rol = rol;
  }

  public Long getOpcionMenuId() {
    return opcionMenuId;
  }

  public void setOpcionMenuId(Long opcionMenuId) {
    this.opcionMenuId = opcionMenuId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RolOpcionMenuId that)) return false;
    return rol == that.rol && Objects.equals(opcionMenuId, that.opcionMenuId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rol, opcionMenuId);
  }
}
