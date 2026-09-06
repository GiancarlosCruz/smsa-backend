package users.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Catálogo de puestos administrativos (Secretaria General, Tesorería, etc.).
 * Qué opciones de menú ve cada cargo es responsabilidad del módulo `menu`,
 * que referencia este id por valor (cargoId), sin relación JPA — mismo
 * criterio que sedeId en UsuarioSedeRol.
 */
@Entity
@Table(name = "cargo")
public class Cargo extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(name = "nombre", nullable = false, unique = true, length = 100)
  public String nombre;

  @Column(name = "descripcion", length = 250)
  public String descripcion;
}