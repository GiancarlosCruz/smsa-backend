package users.application;

import users.api.generated.model.AsignarRolRequest;
import users.domain.model.Rol;
import users.security.ContextoAcceso;

public interface RolSedeService {

 void asignar(Long usuarioId, AsignarRolRequest request, ContextoAcceso contexto);

 void revocar(Long usuarioId, Rol rol, Long sedeId, ContextoAcceso contexto);
}