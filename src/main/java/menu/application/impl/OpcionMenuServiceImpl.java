package menu.application.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import menu.api.generated.model.ActualizarOpcionMenuRequest;
import menu.api.generated.model.CrearOpcionMenuRequest;
import menu.application.OpcionMenuService;
import menu.domain.exception.AsignacionMenuInvalidaException;
import menu.domain.exception.MenuOpcionCodigoDuplicadoException;
import menu.domain.exception.MenuOpcionEnUsoException;
import menu.domain.exception.MenuOpcionNoEncontradaException;
import menu.domain.model.OpcionMenu;
import menu.domain.repository.OpcionMenuRepository;
import org.hibernate.exception.ConstraintViolationException;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class OpcionMenuServiceImpl implements OpcionMenuService {

  private final OpcionMenuRepository opcionMenuRepository;

  @Inject
  public OpcionMenuServiceImpl(OpcionMenuRepository opcionMenuRepository) {
    this.opcionMenuRepository = opcionMenuRepository;
  }

  public List<OpcionMenu> listar() {
    return opcionMenuRepository.listAll();
  }

  @Transactional
  @Override
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
    opcion.opcionPadre = Optional.ofNullable(request.getOpcionPadreId())
            .map(this::resolverPadre)
            .map(padre -> padre.id)
            .orElse(null);
    opcionMenuRepository.persist(opcion);
    return opcion;
  }

  @Transactional
  @Override
  public OpcionMenu actualizar(Long id, ActualizarOpcionMenuRequest request) {
    OpcionMenu opcion = opcionMenuRepository.findByIdOptional(id)
            .orElseThrow(() -> new MenuOpcionNoEncontradaException(id));

    if (request.getEtiqueta() != null) opcion.etiqueta = request.getEtiqueta();
    if (request.getIcono() != null) opcion.icono = request.getIcono();
    if (request.getRuta() != null) opcion.rutaFrontend = request.getRuta();
    if (request.getOrden() != null) opcion.orden = request.getOrden();
    if (request.getOpcionPadreId() != null) {
      OpcionMenu padre = resolverPadre(request.getOpcionPadreId());
      if (padre != null) {
        if (padre.id.equals(opcion.id)) {
          throw new IllegalArgumentException("Una opción no puede ser padre de sí misma");
        }
        opcion.opcionPadre = padre.id;
      } else {
        opcion.opcionPadre = null;
      }
    }
    return opcion;
  }

  /**
   * Si existen opciones hijas apuntando a esta (opcion_padre_id), el
   * delete falla por integridad referencial — se traduce a un error de
   * negocio claro en vez de dejar pasar el 500 genérico.
   */
  @Transactional
  @Override
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

  @Override
  public List<OpcionMenu> obtenerOpcionesDeCargo(Long cargoId) {
    return opcionMenuRepository.buscarPorCargoId(cargoId);
  }

  @Override
  public List<OpcionMenu> obtenerOpcionesDeRol(String rol) {
    return opcionMenuRepository.buscarPorRol(rol);
  }

  @Transactional
  @Override
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
  @Override
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
