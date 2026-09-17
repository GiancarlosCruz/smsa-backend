package users.config;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalDate;

@ApplicationScoped
@Getter
public class UserSeedProperties {

  @ConfigProperty(name = "seed.admin.enabled")
  boolean seedAdminEnabled;

  @ConfigProperty(name = "seed.admin.tipo-documento")
  String seedAdminTipoDocumento;

  @ConfigProperty(name = "seed.admin.numero-documento")
  String seedAdminNumeroDocumento;

  @ConfigProperty(name = "seed.admin.nombres")
  String seedAdminNombres;

  @ConfigProperty(name = "seed.admin.apellido-paterno")
  String seedAdminApellidoPaterno;

  @ConfigProperty(name = "seed.admin.email")
  String seedAdminEmail;

  @ConfigProperty(name = "seed.admin.fecha-nacimiento")
  LocalDate seedAdminFechaNacimiento;
}
