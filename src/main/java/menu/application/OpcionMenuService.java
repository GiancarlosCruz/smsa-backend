package menu.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import menu.api.generated.model.ActualizarOpcionMenuRequest;
import menu.api.generated.model.CrearOpcionMenuRequest;
import menu.domain.exception.AsignacionMenuInvalidaException;
import menu.domain.exception.MenuOpcionCodigoDuplicadoException;
import menu.domain.exception.MenuOpcionEnUsoException;
import menu.domain.exception.MenuOpcionNoEncontradaException;
import menu.domain.model.OpcionMenu;
import menu.domain.repository.OpcionMenuRepository;
import org.hibernate.exception.ConstraintViolationException;

import java.util.List;

@ApplicationScoped
public class OpcionMenuService {

  @Inject OpcionMenuRepository opcionMenuRepository;

  public List<OpcionMenu> listar() {
    return opcionMenuRepository.listAll();
  }

  @Transactional
  public OpcionMenu crear(CrearOpcionMenuRequest request) {
    if (opcionMenuRepository.existeCodigo(request.getCodigo())) {
      throw new MenuOpcionCodigoDuplicadoException(request.getCodigo());
    }
    OpcionMenu opcion = new OpcionMenu();
    opcion.codigo = request.getCodigo();
    opcion.etiqueta = request.getEtiqueta();
    opcion.icono = request.getIcono();
    opcion.rutaFrontend = request.getRuta();
    opcion.orden = request.getOrden() != null ? request.getOrden() : 0;
    opcion.opcionPadre = request.getOpcionPadreId() != null ? resolverPadre(request.getOpcionPadreId()).id : null;
    opcionMenuRepository.persist(opcion);
    return opcion;
  }

  @Transactional
  public OpcionMenu actualizar(Long id, ActualizarOpcionMenuRequest request) {
    OpcionMenu opcion = opcionMenuRepository.findByIdOptional(id)
            .orElseThrow(() -> new MenuOpcionNoEncontradaException(id));

    if (request.getEtiqueta() != null) opcion.etiqueta = request.getEtiqueta();
    if (request.getIcono() != null) opcion.icono = request.getIcono();
    if (request.getRuta() != null) opcion.rutaFrontend = request.getRuta();
    if (request.getOrden() != null) opcion.orden = request.getOrden();
    if (request.getOpcionPadreId() != null) {
      OpcionMenu padre = resolverPadre(request.getOpcionPadreId());
      if (padre != null && padre.id.equals(opcion.id)) {
        throw new IllegalArgumentException("Una opción no puede ser padre de sí misma");
      }
      opcion.opcionPadre = padre.id;
    }
    return opcion;
  }

  /**
   * Si existen opciones hijas apuntando a esta (opcion_padre_id), el
   * delete falla por integridad referencial — se traduce a un error de
   * negocio claro en vez de dejar pasar el 500 genérico.
   */
  @Transactional
  public void eliminar(Long id) {
    OpcionMenu opcion = opcionMenuRepository.findByIdOptional(id)
            .orElseThrow(() -> new MenuOpcionNoEncontradaException(id));
    try {
      opcionMenuRepository.delete(opcion);
      opcionMenuRepository.flush();
    } catch (ConstraintViolationException e) {
      throw new MenuOpcionEnUsoException(id);
    }
  }

  public List<OpcionMenu> obtenerOpcionesDeCargo(Long cargoId) {
    return opcionMenuRepository.buscarPorCargoId(cargoId);
  }

  public List<OpcionMenu> obtenerOpcionesDeRol(String rol) {
    return opcionMenuRepository.buscarPorRol(rol);
  }

  @Transactional
  public void asignarOpcionesARol(String rol, List<Long> opcionesIds) {
    try {
      opcionMenuRepository.reemplazarOpcionesDeRol(rol, opcionesIds);
    } catch (ConstraintViolationException e) {
      throw new AsignacionMenuInvalidaException(rol);
    }
  }

  /**
   * Sin relación JPA entre módulos, cargoId se valida a nivel de BD (la FK
   * física de cargo_opcion_menu.cargo_id) — si cargoId o algún opcionId no
   * existe, se traduce la violación a un error de negocio.
   */
  @Transactional
  public void asignarOpcionesACargo(Long cargoId, List<Long> opcionesIds) {
    try {
      opcionMenuRepository.reemplazarOpcionesDeCargo(cargoId, opcionesIds);
    } catch (ConstraintViolationException e) {
      throw new AsignacionMenuInvalidaException(cargoId);
    }
  }

  private OpcionMenu resolverPadre(Long opcionPadreId) {
    if (opcionPadreId == null) {
      return null;
    }
    return opcionMenuRepository.findByIdOptional(opcionPadreId)
            .orElseThrow(() -> new MenuOpcionNoEncontradaException(opcionPadreId));
  }
}