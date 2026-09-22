package estudiantes.domain.exception;

public class EstudianteNoEncontradoException extends RuntimeException {
  public EstudianteNoEncontradoException(Long id) {
    super("Estudiante no encontrado con ID: " + id);
  }


}
