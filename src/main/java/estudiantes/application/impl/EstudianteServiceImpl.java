package estudiantes.application.impl;

import estudiantes.api.generated.model.ActualizarEstudianteRequest;
import estudiantes.api.generated.model.EstudiantePageResponse;
import estudiantes.api.generated.model.EstudianteResponse;
import estudiantes.api.generated.model.RegistrarEstudianteRequest;
import estudiantes.application.EstudianteService;
import estudiantes.domain.model.Estudiante;
import estudiantes.domain.model.TipoRegistro;
import estudiantes.domain.repository.EstudianteRepository;
import estudiantes.domain.exception.EstudianteYaExisteException;
import estudiantes.domain.exception.EstudianteNoEncontradoException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class EstudianteServiceImpl implements EstudianteService {

  private final EstudianteRepository repository;

  public EstudianteServiceImpl(EstudianteRepository repository) {
    this.repository = repository;
  }

  @Transactional
  @Override
  public EstudianteResponse registrar(RegistrarEstudianteRequest registrarEstudianteRequest) {
    if (repository.existeCodigoEstudiante(registrarEstudianteRequest.getCodigoEstudiante())) {
      throw new EstudianteYaExisteException(registrarEstudianteRequest.getCodigoEstudiante());
    }

    Estudiante estudiante = new Estudiante();
    estudiante.setUsuarioId(registrarEstudianteRequest.getUsuarioId());
    estudiante.setCodigoEstudiante(registrarEstudianteRequest.getCodigoEstudiante());
    estudiante.setProgramaId(registrarEstudianteRequest.getProgramaId());
    estudiante.setPeriodoIngreso(registrarEstudianteRequest.getPeriodoIngreso());
    estudiante.setSemestre(registrarEstudianteRequest.getSemestre().shortValue());
    estudiante.setTipoRegistro(TipoRegistro.valueOf(registrarEstudianteRequest.getTipoRegistro().name()));
    estudiante.setDireccion(registrarEstudianteRequest.getDireccion());
    estudiante.setContactoEmergenciaNombre(registrarEstudianteRequest.getContactoEmergenciaNombre());
    estudiante.setContactoEmergenciaTelefono(registrarEstudianteRequest.getContactoEmergenciaTelefono());

    repository.persist(estudiante);
    return mapear(estudiante);
  }

  public EstudiantePageResponse listar(Long programaId, String periodoIngreso, String q, Integer page, Integer size) {

    return new EstudiantePageResponse();

  }

  @Override
  public EstudianteResponse obtenerPorUsuarioId(Long usuarioId) {
    return mapear(repository.findByUsuarioId(usuarioId)
            .orElseThrow(() -> new EstudianteNoEncontradoException(usuarioId)));
  }

  @Override
  public EstudianteResponse obtenerPorId(Long id) {
    return mapear(repository.findByIdOptional(id)
            .orElseThrow(() -> new EstudianteNoEncontradoException(id)));
  }

  @Transactional
  @Override
  public EstudianteResponse actualizar(Long id, ActualizarEstudianteRequest actualizarEstudianteRequest) {
    var estudiante = obtenerPorId(id);

    if (estudiante.getProgramaId() != null && !estudiante.getProgramaId().equals(actualizarEstudianteRequest.getProgramaId())) {
      throw new IllegalArgumentException("No se puede cambiar el programa de un estudiante existente.");
    }

    if (actualizarEstudianteRequest.getProgramaId() != null) {
      estudiante.setProgramaId(actualizarEstudianteRequest.getProgramaId());
    }
    if (actualizarEstudianteRequest.getPeriodoIngreso() != null) {
      estudiante.setPeriodoIngreso(actualizarEstudianteRequest.getPeriodoIngreso());
    }
    if (actualizarEstudianteRequest.getSemestre() != null) {
      estudiante.setSemestre(actualizarEstudianteRequest.getSemestre());
    }
    if (actualizarEstudianteRequest.getDireccion() != null) {
      estudiante.setDireccion(actualizarEstudianteRequest.getDireccion());
    }
    if (actualizarEstudianteRequest.getContactoEmergenciaNombre() != null) {
      estudiante.setContactoEmergenciaNombre(actualizarEstudianteRequest.getContactoEmergenciaNombre());
    }
    if (actualizarEstudianteRequest.getContactoEmergenciaTelefono() != null) {
      estudiante.setContactoEmergenciaTelefono(actualizarEstudianteRequest.getContactoEmergenciaTelefono());
    }

    return estudiante;
  }

  private EstudianteResponse mapear(Estudiante estudiante) {
    return new EstudianteResponse()
            .id(estudiante.getId())
            .usuarioId(estudiante.getUsuarioId())
            .codigoEstudiante(estudiante.getCodigoEstudiante())
            .programaId(estudiante.getProgramaId())
            .periodoIngreso(estudiante.getPeriodoIngreso())
            .semestre(estudiante.getSemestre().intValue())
            .tipoRegistro(
                    estudiantes.api.generated.model.TipoRegistro.valueOf(estudiante.getTipoRegistro().name()))
            .direccion(estudiante.getDireccion())
            .contactoEmergenciaNombre(estudiante.getContactoEmergenciaNombre())
            .contactoEmergenciaTelefono(estudiante.getContactoEmergenciaTelefono());
  }

}