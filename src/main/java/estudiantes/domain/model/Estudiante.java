package estudiantes.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Entity
@Getter
@Setter
@ToString
@Table(name = "estudiante")
public class Estudiante {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "usuario_id", nullable = false, unique = true)
  private Long usuarioId;

  @Column(name = "codigo_estudiante", nullable = false, unique = true, length = 20)
  private String codigoEstudiante;

  @Column(name = "programa_id", nullable = false)
  private Long programaId;

  @Column(name = "periodo_ingreso", nullable = false, length = 10)
  private String periodoIngreso;

  @Column(nullable = false)
  private Short semestre;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_registro", nullable = false, length = 20)
  private TipoRegistro tipoRegistro;

  @Column(name = "direccion", length = 250)
  private String direccion;

  @Column(name = "contacto_emergencia_nombre", length = 150)
  private String contactoEmergenciaNombre;

  @Column(name = "contacto_emergencia_telefono", length = 20)
  private String contactoEmergenciaTelefono;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  void onCreate() {
    var now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }

}