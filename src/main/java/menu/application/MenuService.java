package menu.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import menu.api.generated.model.OpcionMenuResponse;
import menu.domain.model.OpcionMenu;
import menu.domain.repository.OpcionMenuRepository;
import users.domain.model.Rol;
import users.security.ContextoAcceso;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Data-driven a propósito: agregar/quitar una opción, reordenar la
 * jerarquía, o cambiar qué rol/cargo ve qué, es una operación sobre datos
 * (rol_opcion_menu / cargo_opcion_menu / opcion_padre_id), nunca un cambio
 * de código ni un deploy.
 *
 * El menú final es la UNIÓN de dos fuentes:
 * - rol_opcion_menu: menú base, aplica a CUALQUIERA de los roles vigentes
 *   del usuario (no solo el "informativo" — un docente que también es
 *   administrativo ve el menú base de AMBOS roles combinado).
 * - cargo_opcion_menu: refinamiento fino, solo aplica si tiene cargo
 *   (ADMINISTRATIVO).
 *
 * Un hijo solo se anida bajo su padre si ambos quedaron en el conjunto
 * combinado. Si un hijo queda sin su padre, no se pierde: aparece como
 * ítem de nivel raíz en vez de desaparecer silenciosamente.
 */
@ApplicationScoped
public class MenuService {

  @Inject OpcionMenuRepository opcionMenuRepository;

  public List<OpcionMenuResponse> obtenerMenuPropio(ContextoAcceso contexto) {
    List<Rol> roles = contexto.roles();
    if (roles == null || roles.isEmpty()) {
      return List.of();
    }

    List<OpcionMenu> porRol = roles.stream()
            .flatMap(rol -> opcionMenuRepository.buscarPorRol(rol.name()).stream())
            .toList();

    List<OpcionMenu> porCargo = contexto.cargoId() != null
            ? opcionMenuRepository.buscarPorCargoId(contexto.cargoId())
            : List.of();

    // Unión sin duplicados, preservando la primera aparición de cada id.
    Map<Long, OpcionMenu> combinadas = new LinkedHashMap<>();
    porRol.forEach(o -> combinadas.putIfAbsent(o.id, o));
    porCargo.forEach(o -> combinadas.putIfAbsent(o.id, o));
    List<OpcionMenu> todas = List.copyOf(combinadas.values());

    Set<Long> idsAsignados = todas.stream().map(o -> o.id).collect(Collectors.toSet());

    return todas.stream()
            .filter(o -> o.opcionPadre == null || !idsAsignados.contains(o.opcionPadre))
            .sorted(Comparator.comparingInt(o -> o.orden))
            .map(raiz -> mapearConHijos(raiz, todas))
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