package users.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import users.api.generated.model.PerfilDocente;
import users.api.generated.model.PerfilEstudiante;
import users.domain.model.DocentePerfil;
import users.domain.model.EstudiantePerfil;

/**
 * Los nombres de campo coinciden 1 a 1 entre entidad y DTO (gradoAcademico,
 * especialidad, tipoContrato, fechaIngreso / codigoEstudiante, programaId,
 * contactoEmergenciaNombre, contactoEmergenciaTelefono, direccion), así que
 * MapStruct resuelve el mapeo sin @Mapping explícito. Si source es null,
 * MapStruct devuelve null (comportamiento por defecto) — UsuarioMapper ya
 * maneja ese caso antes de asignar al campo perfilDocente/perfilEstudiante.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface PerfilMapper {

  PerfilDocente aDto(DocentePerfil docentePerfil);

  PerfilEstudiante aDto(EstudiantePerfil estudiantePerfil);
}