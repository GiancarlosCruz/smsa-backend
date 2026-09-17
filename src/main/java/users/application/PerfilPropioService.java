package users.application;

import users.api.generated.model.ActualizarPerfilPropioRequest;
import users.api.generated.model.UsuarioPerfilResponse;
import users.security.ContextoAcceso;

public interface PerfilPropioService {

 UsuarioPerfilResponse obtenerPropio(ContextoAcceso contexto);

 void actualizarPropio(ContextoAcceso contexto, ActualizarPerfilPropioRequest request);

 void cambiarPasswordPropio(ContextoAcceso contexto, String passwordNueva);
}