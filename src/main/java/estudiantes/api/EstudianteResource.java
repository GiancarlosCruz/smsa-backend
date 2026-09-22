package estudiantes.api;

import estudiantes.api.generated.EstudiantesApi;
import estudiantes.api.generated.model.ActualizarEstudianteRequest;
import estudiantes.api.generated.model.EstudiantePageResponse;
import estudiantes.api.generated.model.EstudianteResponse;
import estudiantes.api.generated.model.RegistrarEstudianteRequest;
import estudiantes.application.EstudianteService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EstudianteResource implements EstudiantesApi {

  private final EstudianteService estudianteService;

  @Inject
  public EstudianteResource (EstudianteService estudianteService) {
    this.estudianteService = estudianteService;
  }

  @Override
  public EstudianteResponse actualizarEstudiante(Long id, ActualizarEstudianteRequest actualizarEstudianteRequest) {
    return estudianteService.actualizar(id, actualizarEstudianteRequest);
  }

  @Override
  public EstudiantePageResponse listarEstudiantes(Long programaId, String periodoIngreso, String q, Integer page, Integer size) {
    return null; //estudianteService.listar(programaId, periodoIngreso, q, page, size);
  }

  @Override
  public EstudianteResponse obtenerEstudiante(Long id) {
    return estudianteService.obtenerPorId(id);
  }

  @Override
  public EstudianteResponse obtenerEstudiantePropio() {
    return null;
  }

  @Override
  public EstudianteResponse registrarEstudiante(RegistrarEstudianteRequest registrarEstudianteRequest) {
    return estudianteService.registrar(registrarEstudianteRequest);
  }

}
