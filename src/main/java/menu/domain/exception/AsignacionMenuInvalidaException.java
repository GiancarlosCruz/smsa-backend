package menu.domain.exception;

/**
 * Se lanza cuando falla una asignación (cargo↔opciones o rol↔opciones) por
 * una referencia inválida. No se distingue cuál id exactamente falló —
 * sería frágil intentar parsear el nombre de la constraint SQL para
 * adivinarlo; el mensaje es honesto sobre esa ambigüedad.
 */
public class AsignacionMenuInvalidaException extends RuntimeException {

  public AsignacionMenuInvalidaException(Long cargoId) {
    super("No se pudo asignar las opciones al cargo " + cargoId
            + ": el cargoId o alguno de los opcionesIds no existe");
  }

  public AsignacionMenuInvalidaException(String rol) {
    super("No se pudo asignar las opciones al rol " + rol
            + ": alguno de los opcionesIds no existe");
  }
}