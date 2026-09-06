package users.api;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import users.api.generated.PerfilPropioApi;
import users.api.generated.model.ActualizarPerfilPropioRequest;
import users.api.generated.model.CambiarPasswordRequest;
import users.api.generated.model.UsuarioPerfilResponse;
import users.application.PerfilPropioService;
import users.security.ContextoAcceso;

@RequestScoped
public class UsuarioResource implements PerfilPropioApi {

  @Inject PerfilPropioService perfilPropioService;
  @Inject ContextoAcceso contextoAcceso;

  @Override
  @RolesAllowed({"ADMIN", "DOCENTE", "ESTUDIANTE", "ADMINISTRATIVO"})
  public UsuarioPerfilResponse obtenerPerfilPropio() {
    return perfilPropioService.obtenerPropio(contextoAcceso);
  }

  @Override
  @RolesAllowed({"ADMIN", "DOCENTE", "ESTUDIANTE", "ADMINISTRATIVO"})
  public UsuarioPerfilResponse actualizarPerfilPropio(ActualizarPerfilPropioRequest request) {
    perfilPropioService.actualizarPropio(contextoAcceso, request);
    return perfilPropioService.obtenerPropio(contextoAcceso);
  }

  @Override
  @RolesAllowed({"ADMIN", "DOCENTE", "ESTUDIANTE", "ADMINISTRATIVO"})
  public void cambiarPasswordPropio(CambiarPasswordRequest request) {
    perfilPropioService.cambiarPasswordPropio(contextoAcceso, request.getPasswordNueva());
  }
}