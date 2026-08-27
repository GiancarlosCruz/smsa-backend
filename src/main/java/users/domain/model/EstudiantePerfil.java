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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

/**
 * Datos de perfil específicos del rol ESTUDIANTE. Comparte clave primaria con
 * Usuario (usuarioId) vía @MapsId, no tiene id propio.
 *
 * programaId es una referencia por id al módulo académico (dueño de Programa),
 * no una relación JPA — mismo criterio de límite entre módulos que sedeId en
 * UsuarioSedeRol.
 */
@Entity
@Table(
        name = "estudiante_perfil",
        uniqueConstraints = @UniqueConstraint(name = "uk_estudiante_codigo", columnNames = "codigo_estudiante")
)
public class EstudiantePerfil extends PanacheEntityBase {

  @Id
  public Long usuarioId;

  @NotNull
  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "usuario_id")
  public Usuario usuario;

  @NotNull
  @Column(name = "codigo_estudiante", nullable = false, length = 20)
  public String codigoEstudiante;

  @Column(name = "programa_id")
  public Long programaId;

  @Column(name = "contacto_emergencia_nombre", length = 150)
  public String contactoEmergenciaNombre;

  @Column(name = "contacto_emergencia_telefono", length = 20)
  public String contactoEmergenciaTelefono;

  @Column(name = "direccion", length = 250)
  public String direccion;
}