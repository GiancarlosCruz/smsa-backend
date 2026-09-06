package menu.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import menu.api.generated.model.ActualizarOpcionMenuRequest;
import menu.api.generated.model.CrearOpcionMenuRequest;
import menu.domain.exception.MenuOpcionCodigoDuplicadoException;
import menu.domain.exception.MenuOpcionNoEncontradaException;
import menu.domain.model.OpcionMenu;
import menu.domain.repository.OpcionMenuRepository;

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

  @Transactional
  public void eliminar(Long id) {
    OpcionMenu opcion = opcionMenuRepository.findByIdOptional(id)
            .orElseThrow(() -> new MenuOpcionNoEncontradaException(id));
    opcionMenuRepository.delete(opcion);
  }

  public List<OpcionMenu> obtenerOpcionesDeCargo(Long cargoId) {
    return opcionMenuRepository.buscarPorCargoId(cargoId);
  }

  @Transactional
  public void asignarOpcionesACargo(Long cargoId, List<Long> opcionesIds) {
    opcionMenuRepository.reemplazarOpcionesDeCargo(cargoId, opcionesIds);
  }

  private OpcionMenu resolverPadre(Long opcionPadreId) {
    if (opcionPadreId == null) {
      return null;
    }
    return opcionMenuRepository.findByIdOptional(opcionPadreId)
            .orElseThrow(() -> new MenuOpcionNoEncontradaException(opcionPadreId));
  }
}