package users.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Datos de perfil específicos del rol DOCENTE. Comparte clave primaria con
 * Usuario (usuarioId) vía @MapsId, no tiene id propio.
 */
@Entity
@Table(name = "docente_perfil")
public class DocentePerfil extends PanacheEntityBase {

  @Id
  public Long usuarioId;

  @NotNull
  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "usuario_id")
  public Usuario usuario;

  @Column(name = "grado_academico", length = 100)
  public String gradoAcademico;

  @Column(name = "especialidad", length = 150)
  public String especialidad;

  @Column(name = "tipo_contrato", length = 50)
  public String tipoContrato;

  @NotNull
  @Column(name = "fecha_ingreso", nullable = false)
  public LocalDate fechaIngreso;
}