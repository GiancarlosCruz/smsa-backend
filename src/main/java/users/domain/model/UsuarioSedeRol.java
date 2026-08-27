package users.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Fuente de verdad de autorización por sede. NO vive en el token de Keycloak:
 * la asignación de sede/rol cambia por semestre y consultarla desde acá
 * evita el problema de claims "congelados" hasta que el token refresque.
 *
 * sedeId es una referencia por id al módulo académico (dueño de la entidad Sede),
 * no una relación JPA — un monolito modular no debe cruzar entidades entre
 * dominios aunque comparta el mismo esquema físico.
 *
 * sedeId == null representa acceso a TODAS las sedes. En este alcance MVP
 * eso solo es válido para rol == ADMIN; RolSedeService debe validarlo al crear.
 */
@Entity
@Table(name = "usuario_sede_rol")
public class UsuarioSedeRol extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  public Usuario usuario;

  @Column(name = "sede_id")
  public Long sedeId;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "rol", nullable = false, length = 20)
  public Rol rol;

  @NotNull
  @Column(name = "fecha_inicio", nullable = false)
  public LocalDate fechaInicio;

  @Column(name = "fecha_fin")
  public LocalDate fechaFin;

  public boolean vigente() {
    LocalDate hoy = LocalDate.now();
    boolean yaInicio = !hoy.isBefore(fechaInicio);
    boolean noHaTerminado = fechaFin == null || !hoy.isAfter(fechaFin);
    return yaInicio && noHaTerminado;
  }

  public boolean esAccesoGlobal() {
    return sedeId == null;
  }
}