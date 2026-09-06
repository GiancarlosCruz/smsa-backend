package menu.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Un ítem de menú del frontend. opcionPadre existe para soportar submenús
 * más adelante — el MenuService actual devuelve una lista plana por
 * simplicidad; no se arma jerarquía todavía.
 */
@Entity
@Table(name = "opcion_menu")
public class OpcionMenu extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(name = "codigo", nullable = false, unique = true, length = 50)
  public String codigo;

  @Column(name = "etiqueta", nullable = false, length = 100)
  public String etiqueta;

  @Column(name = "icono", length = 50)
  public String icono;

  @Column(name = "ruta_frontend", length = 150)
  public String rutaFrontend;

  @Column(name = "orden", nullable = false)
  public int orden;

  //@ManyToOne(fetch = FetchType.LAZY)
  @Column(name = "opcion_padre_id")
  public Long opcionPadre;


}