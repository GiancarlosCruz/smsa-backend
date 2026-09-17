package users.application;

import users.api.generated.model.ActualizarUsuarioRequest;
import users.api.generated.model.CrearUsuarioRequest;
import users.domain.model.EstadoUsuario;
import users.domain.model.Usuario;
import users.security.ContextoAcceso;

public interface UsuarioService {

 Usuario crear(CrearUsuarioRequest request, ContextoAcceso contexto);

 Usuario actualizar(Long usuarioId, ActualizarUsuarioRequest request, ContextoAcceso contexto);

 void cambiarEstado(Long usuarioId, EstadoUsuario nuevoEstado, ContextoAcceso contexto);

 void sembrarAdminSiNoExiste();
}