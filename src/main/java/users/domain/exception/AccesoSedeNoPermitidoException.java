package users.domain.exception;

/**
 * Se lanza cuando un admin de sede intenta operar sobre un recurso de una
 * sede fuera de sus sedesPermitidas (ContextoAcceso). No se usa para filtrar
 * listados (eso se filtra silenciosamente en la query); se usa cuando el
 * usuario pide explícitamente un recurso puntual (por id) al que no tiene acceso.
 */
public class AccesoSedeNoPermitidoException extends RuntimeException {

  public AccesoSedeNoPermitidoException(Long sedeId) {
    super("No tienes permiso para operar sobre la sede " + sedeId);
  }
}