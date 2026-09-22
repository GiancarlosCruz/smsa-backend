package estudiantes.application;

import estudiantes.api.generated.model.ActualizarEstudianteRequest;
import estudiantes.api.generated.model.EstudianteResponse;
import estudiantes.api.generated.model.RegistrarEstudianteRequest;

public interface EstudianteService {
  EstudianteResponse registrar(RegistrarEstudianteRequest registrarEstudianteRequest);
  EstudianteResponse obtenerPorUsuarioId(Long usuarioId);
  EstudianteResponse obtenerPorId(Long id);
  EstudianteResponse actualizar(Long id, ActualizarEstudianteRequest actualizarEstudianteRequest);
}
