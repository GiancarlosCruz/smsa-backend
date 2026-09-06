package users.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Datos de perfil específicos del rol ADMINISTRATIVO. Mismo patrón que
 * DocentePerfil/EstudiantePerfil: usuarioId compartido vía @MapsId.
 *
 * cargo determina qué opciones de menú ve esta persona (ver Cargo.opciones) —
 * es la pieza central del menú dinámico.
 */
@Entity
@Table(name = "administrativo_perfil")
public class AdministrativoPerfil extends PanacheEntityBase {

  @Id
  public Long usuarioId;

  @NotNull
  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "usuario_id")
  public Usuario usuario;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cargo_id", nullable = false)
  public Cargo cargo;

  @Column(name = "area_administrativa", length = 150)
  public String areaAdministrativa;

  @Column(name = "condicion", length = 20)
  public String condicion;
}