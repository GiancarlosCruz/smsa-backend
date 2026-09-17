package users.infrastructure;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import users.application.UsuarioService;

/**
 * Dispara la siembra del admin inicial al arrancar la app. Corre después de
 * que Flyway ya aplicó las migraciones (el StartupEvent de la app se
 * dispara después de que el datasource/ORM terminan de inicializar).
 */
@ApplicationScoped
public class AdminBootstrapper {

  @Inject UsuarioService usuarioService;

  void onStart(@Observes StartupEvent event) {
    usuarioService.sembrarAdminSiNoExiste();
  }
}