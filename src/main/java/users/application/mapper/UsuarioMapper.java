package users.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import users.api.generated.model.UsuarioAdminDetalle;
import users.api.generated.model.UsuarioAdminListItem;
import users.api.generated.model.UsuarioPerfilResponse;
import users.domain.model.*;

import java.util.List;

/**
 * rol, sedes, docentePerfil y estudiantePerfil NO viven en Usuario (ver
 * UsuarioSedeRol / DocentePerfil / EstudiantePerfil), por eso los tres
 * métodos toman esos datos como parámetros aparte en vez de mapear solo
 * desde la entidad Usuario. El llamador (UsuarioAdminResource,
 * PerfilPropioService) sigue siendo responsable de resolverlos antes de
 * invocar el mapper.
 * La conversión Rol -> generated.model.Rol y EstadoUsuario ->
 * generated.model.EstadoUsuario la resuelve MapStruct automáticamente: son
 * enums distintos pero con las mismas constantes (ADMIN/DOCENTE/ESTUDIANTE,
 * ACTIVO/INACTIVO/SUSPENDIDO), y MapStruct genera el mapeo por nombre sin
 * necesidad de declararlo.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.JAKARTA_CDI, // o directamente "jakarta-cdi"
        uses = { PerfilMapper.class }
)
public interface UsuarioMapper {

  @Mapping(target = "id", source = "usuario.id")
  @Mapping(target = "nombres", source = "usuario.nombres")
  @Mapping(target = "apellidoPaterno", source = "usuario.apellidoPaterno")
  @Mapping(target = "apellidoMaterno", source = "usuario.apellidoMaterno")
  @Mapping(target = "email", source = "usuario.email")
  @Mapping(target = "telefono", source = "usuario.telefono")
  @Mapping(target = "estado", source = "usuario.estado")
  @Mapping(target = "rol", source = "rol")
  @Mapping(target = "sedes", source = "sedes")
  @Mapping(target = "removeSedesItem", ignore = true)
  @Mapping(target = "roles", ignore = true)
  @Mapping(target = "removeRolesItem", ignore = true)
  UsuarioAdminListItem aListItem(Usuario usuario, Rol rol, List<Long> sedes);

  @Mapping(target = "id", source = "usuario.id")
  @Mapping(target = "nombres", source = "usuario.nombres")
  @Mapping(target = "apellidoPaterno", source = "usuario.apellidoPaterno")
  @Mapping(target = "apellidoMaterno", source = "usuario.apellidoMaterno")
  @Mapping(target = "email", source = "usuario.email")
  @Mapping(target = "telefono", source = "usuario.telefono")
  @Mapping(target = "estado", source = "usuario.estado")
  @Mapping(target = "rol", source = "rol")
  @Mapping(target = "sedes", source = "sedes")
  @Mapping(target = "perfilDocente", source = "docentePerfil")
  @Mapping(target = "perfilEstudiante", source = "estudiantePerfil")
  @Mapping(target = "perfilAdministrativo", source = "administrativoPerfil")
  @Mapping(target = "removeSedesItem", ignore = true)
  @Mapping(target = "roles", ignore = true)
  @Mapping(target = "removeRolesItem", ignore = true)
  @Mapping(target = "perfilAdministrativo.cargoId", source = "administrativoPerfil.cargo.id") // O el campo de origen que corresponda
  @Mapping(target = "perfilAdministrativo.cargoNombre", source = "administrativoPerfil.cargo.nombre")
  @Mapping(target = "perfilAdministrativo.areaAdministrativa", source = "administrativoPerfil.areaAdministrativa")
  @Mapping(target = "perfilAdministrativo.condicion", source = "administrativoPerfil.condicion")
  UsuarioAdminDetalle aDetalle(Usuario usuario, Rol rol, List<Long> sedes,
                               DocentePerfil docentePerfil, EstudiantePerfil estudiantePerfil, AdministrativoPerfil administrativoPerfil);

  @Mapping(target = "id", source = "usuario.id")
  @Mapping(target = "nombres", source = "usuario.nombres")
  @Mapping(target = "apellidoPaterno", source = "usuario.apellidoPaterno")
  @Mapping(target = "apellidoMaterno", source = "usuario.apellidoMaterno")
  @Mapping(target = "email", source = "usuario.email")
  @Mapping(target = "telefono", source = "usuario.telefono")
  @Mapping(target = "estado", source = "usuario.estado")
  @Mapping(target = "rol", source = "rol")
  @Mapping(target = "sedes", source = "sedes")
  @Mapping(target = "perfilDocente", source = "docentePerfil")
  @Mapping(target = "perfilEstudiante", source = "estudiantePerfil")
  @Mapping(target = "perfilAdministrativo", source = "administrativoPerfil")
  @Mapping(target = "removeSedesItem", ignore = true)
  @Mapping(target = "roles", ignore = true)
  @Mapping(target = "removeRolesItem", ignore = true)
  @Mapping(target = "perfilAdministrativo.cargoId", source = "administrativoPerfil.cargo.id") // O el campo de origen que corresponda
  @Mapping(target = "perfilAdministrativo.cargoNombre", source = "administrativoPerfil.cargo.nombre")
  @Mapping(target = "perfilAdministrativo.areaAdministrativa", source = "administrativoPerfil.areaAdministrativa")
  @Mapping(target = "perfilAdministrativo.condicion", source = "administrativoPerfil.condicion")
  UsuarioPerfilResponse aPerfilPropio(Usuario usuario, Rol rol, List<Long> sedes,
                                      DocentePerfil docentePerfil, EstudiantePerfil estudiantePerfil, AdministrativoPerfil administrativoPerfil);
}