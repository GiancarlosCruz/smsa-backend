package users.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import users.domain.model.EstadoUsuario;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Identidad y datos base del usuario en la app.
 * La fuente de verdad de credenciales/login es Keycloak (keycloakId = "sub" del token).
 * La fuente de verdad de autorización por sede es {@link UsuarioSedeRol}, no esta entidad.
 */
@Entity
@Table(
        name = "usuario",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_usuario_keycloak_id", columnNames = "keycloak_id"),
                @UniqueConstraint(name = "uk_usuario_documento", columnNames = {"tipo_documento", "numero_documento"})
        }
)
public class Usuario extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @NotNull
  @Column(name = "keycloak_id", nullable = false, updatable = false)
  public UUID keycloakId;

  @NotNull
  @Column(name = "tipo_documento", nullable = false, length = 20)
  public String tipoDocumento;

  @NotNull
  @Column(name = "numero_documento", nullable = false, length = 20)
  public String numeroDocumento;

  @NotNull
  @Column(name = "nombres", nullable = false, length = 150)
  public String nombres;

  @NotNull
  @Column(name = "apellido_paterno", nullable = false, length = 100)
  public String apellidoPaterno;

  @Column(name = "apellido_materno", length = 100)
  public String apellidoMaterno;

  @Email
  @NotNull
  @Column(name = "email", nullable = false, length = 150)
  public String email;

  @Column(name = "telefono", length = 20)
  public String telefono;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "estado", nullable = false, length = 20)
  public EstadoUsuario estado;

  @CreationTimestamp
  @Column(name = "fecha_creacion", nullable = false, updatable = false)
  public LocalDateTime fechaCreacion;

  @UpdateTimestamp
  @Column(name = "fecha_actualizacion")
  public LocalDateTime fechaActualizacion;

  @Column(name = "creado_por", length = 100)
  public String creadoPor;

  @Column(name = "actualizado_por", length = 100)
  public String actualizadoPor;

  public String nombreCompleto() {
    return apellidoMaterno == null
            ? "%s %s".formatted(nombres, apellidoPaterno)
            : "%s %s %s".formatted(nombres, apellidoPaterno, apellidoMaterno);
  }
}