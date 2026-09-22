package estudiantes.domain.exception;

public class EstudianteYaExisteException extends RuntimeException {
  public EstudianteYaExisteException(String codigoEstudiante) {
    super("Estudiante ya existe con CODIGO: " + codigoEstudiante);
  }
}
