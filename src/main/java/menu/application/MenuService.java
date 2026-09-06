package menu.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import menu.api.generated.model.OpcionMenuResponse;
import menu.domain.model.OpcionMenu;
import menu.domain.repository.OpcionMenuRepository;
import users.security.ContextoAcceso;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Data-driven a propósito: agregar/quitar una opción o reordenar la
 * jerarquía es una operación sobre datos (cargo_opcion_menu / opcion_padre_id),
 * nunca un cambio de código ni un deploy.
 *
 * Un hijo solo se anida bajo su padre si AMBOS están asignados al cargo en
 * cargo_opcion_menu. Si asignas un hijo pero no su padre (por descuido al
 * armar la asignación), no se pierde: aparece como ítem de nivel raíz en
 * vez de desaparecer silenciosamente.
 */
@ApplicationScoped
public class MenuService {

  @Inject OpcionMenuRepository opcionMenuRepository;

  public List<OpcionMenuResponse> obtenerMenuPropio(ContextoAcceso contexto) {
    if (contexto.cargoId() == null) {
      return List.of();
    }

    List<OpcionMenu> asignadas = opcionMenuRepository.buscarPorCargoId(contexto.cargoId());
    Set<Long> idsAsignados = asignadas.stream().map(o -> o.id).collect(Collectors.toSet());

    return asignadas.stream()
            .filter(o -> o.opcionPadre == null || !idsAsignados.contains(o.opcionPadre))
            .sorted(Comparator.comparingInt(o -> o.orden))
            .map(raiz -> mapearConHijos(raiz, asignadas))
            .toList();
  }

  private OpcionMenuResponse mapearConHijos(OpcionMenu opcion, List<OpcionMenu> todas) {
    OpcionMenuResponse dto = mapear(opcion);
    List<OpcionMenuResponse> hijos = todas.stream()
            .filter(o -> o.opcionPadre != null && o.opcionPadre.equals(opcion.id))
            .sorted(Comparator.comparingInt(o -> o.orden))
            .map(hijo -> mapearConHijos(hijo, todas))
            .toList();
    if (!hijos.isEmpty()) {
      dto.setHijos(hijos);
    }
    return dto;
  }

  private OpcionMenuResponse mapear(OpcionMenu opcion) {
    OpcionMenuResponse dto = new OpcionMenuResponse();
    dto.setId(opcion.id);
    dto.setCodigo(opcion.codigo);
    dto.setEtiqueta(opcion.etiqueta);
    dto.setIcono(opcion.icono);
    dto.setRuta(opcion.rutaFrontend);
    dto.setOpcionPadreId(opcion.opcionPadre != null ? opcion.opcionPadre : null);
    return dto;
  }
}