package users.api;

import io.quarkus.panache.common.Page;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import users.api.generated.GestionUsuariosApi;
import users.api.generated.model.*;
import users.application.RolSedeService;
import users.application.UsuarioService;
import users.domain.exception.AccesoSedeNoPermitidoException;
import users.domain.exception.UsuarioNoEncontradoException;
import users.domain.repository.AdministrativoPerfilRepository;
import users.domain.repository.DocentePerfilRepository;
import users.domain.repository.EstudiantePerfilRepository;
import users.domain.repository.UsuarioRepository;
import users.domain.repository.UsuarioSedeRolRepository;
import users.security.ContextoAcceso;

import java.util.List;
import java.util.Objects;

@RequestScoped
@RolesAllowed("ADMIN")
public class UsuarioAdminResource implements GestionUsuariosApi {

  @Inject UsuarioService usuarioService;
  @Inject RolSedeService rolSedeService;
  @Inject UsuarioRepository usuarioRepository;
  @Inject UsuarioSedeRolRepository usuarioSedeRolRepository;
  @Inject DocentePerfilRepository docentePerfilRepository;
  @Inject EstudiantePerfilRepository estudiantePerfilRepository;
  @Inject AdministrativoPerfilRepository administrativoPerfilRepository;
  @Inject ContextoAcceso contextoAcceso;

  @Override
  public UsuarioAdminListPage listarUsuarios(Long sedeId, Rol rol, EstadoUsuario estado,
                                             String q, Integer page, Integer size) {
    List<Long> sedesFiltro = resolverSedesFiltro(sedeId);
    var rolDominio = rol != null ? users.domain.model.Rol.valueOf(rol.name()) : null;
    var estadoDominio = estado != null ? users.domain.model.EstadoUsuario.valueOf(estado.name()) : null;

    int paginaSolicitada = page != null ? page : 0;
    int tamanioSolicitado = size != null ? size : 20;
    Page paginaPanache = Page.of(paginaSolicitada, tamanioSolicitado);

    var usuarios = usuarioRepository.listarParaAdmin(sedesFiltro, rolDominio, estadoDominio, q, paginaPanache);

    UsuarioAdminListPage response = new UsuarioAdminListPage();
    response.setContenido(usuarios.stream().map(this::mapearListItem).toList());
    response.setPagina(paginaSolicitada);
    response.setTamanioPagina(tamanioSolicitado);
    // TODO: total real filtrado (count con los mismos criterios), no el count global.
    response.setTotalElementos((int) usuarioRepository.count());
    return response;
  }

  @Override
  public UsuarioAdminDetalle crearUsuario(CrearUsuarioRequest request) {
    var usuario = usuarioService.crear(request, contextoAcceso);
    return mapearDetalle(usuario);
  }

  @Override
  public UsuarioAdminDetalle obtenerUsuario(Long id) {
    var usuario = usuarioRepository.findByIdOptional(id)
            .orElseThrow(() -> new UsuarioNoEncontradoException(id));
    return mapearDetalle(usuario);
  }

  @Override
  public UsuarioAdminDetalle actualizarUsuario(Long id, ActualizarUsuarioRequest request) {
    var usuario = usuarioService.actualizar(id, request, contextoAcceso);
    return mapearDetalle(usuario);
  }

  @Override
  public void cambiarEstadoUsuario(Long id, CambiarEstadoUsuarioRequest cambiarEstadoUsuarioRequest) {
    usuarioService.cambiarEstado(id, users.domain.model.EstadoUsuario.valueOf(cambiarEstadoUsuarioRequest.getEstado().name()), contextoAcceso);
  }

  @Override
  public void asignarRolSede(Long id, AsignarRolRequest request) {
    rolSedeService.asignar(id, request, contextoAcceso);
  }

  private List<Long> resolverSedesFiltro(Long sedeIdSolicitada) {
    if (sedeIdSolicitada == null) {
      return contextoAcceso.sedesPermitidas();
    }
    if (!contextoAcceso.tieneAccesoASede(sedeIdSolicitada)) {
      throw new AccesoSedeNoPermitidoException(sedeIdSolicitada);
    }
    return List.of(sedeIdSolicitada);
  }

  private UsuarioAdminListItem mapearListItem(users.domain.model.Usuario usuario) {
    UsuarioAdminListItem item = new UsuarioAdminListItem();
    item.setId(usuario.id);
    item.setNombres(usuario.nombres);
    item.setApellidoPaterno(usuario.apellidoPaterno);
    item.setApellidoMaterno(usuario.apellidoMaterno);
    item.setEmail(usuario.email);
    item.setTelefono(usuario.telefono);
    item.setEstado(EstadoUsuario.valueOf(usuario.estado.name()));

    var asignaciones = usuarioSedeRolRepository.vigentesDe(usuario.id);
    asignaciones.stream().findFirst().ifPresent(a -> item.setRol(Rol.valueOf(a.rol.name())));
    item.setSedes(asignaciones.stream().map(a -> a.sedeId).filter(Objects::nonNull).toList());
    return item;
  }

  private UsuarioAdminDetalle mapearDetalle(users.domain.model.Usuario usuario) {
    UsuarioAdminDetalle detalle = new UsuarioAdminDetalle();
    detalle.setId(usuario.id);
    detalle.setNombres(usuario.nombres);
    detalle.setApellidoPaterno(usuario.apellidoPaterno);
    detalle.setApellidoMaterno(usuario.apellidoMaterno);
    detalle.setEmail(usuario.email);
    detalle.setTelefono(usuario.telefono);
    detalle.setEstado(EstadoUsuario.valueOf(usuario.estado.name()));

    var asignaciones = usuarioSedeRolRepository.vigentesDe(usuario.id);
    asignaciones.stream().findFirst().ifPresent(a -> detalle.setRol(Rol.valueOf(a.rol.name())));
    detalle.setSedes(asignaciones.stream().map(a -> a.sedeId).filter(Objects::nonNull).toList());

    boolean esDocente = asignaciones.stream().anyMatch(a -> a.rol == users.domain.model.Rol.DOCENTE);
    boolean esEstudiante = asignaciones.stream().anyMatch(a -> a.rol == users.domain.model.Rol.ESTUDIANTE);
    boolean esAdministrativo = asignaciones.stream().anyMatch(a -> a.rol == users.domain.model.Rol.ADMINISTRATIVO);

    if (esDocente) {
      docentePerfilRepository.buscarPorUsuarioId(usuario.id).ifPresent(dp -> {
        PerfilDocente dto = new PerfilDocente();
        dto.setGradoAcademico(dp.gradoAcademico);
        dto.setEspecialidad(dp.especialidad);
        dto.setTipoContrato(dp.tipoContrato);
        dto.setFechaIngreso(dp.fechaIngreso);
        detalle.setPerfilDocente(dto);
      });
    }
    if (esEstudiante) {
      estudiantePerfilRepository.buscarPorUsuarioId(usuario.id).ifPresent(ep -> {
        PerfilEstudiante dto = new PerfilEstudiante();
        dto.setCodigoEstudiante(ep.codigoEstudiante);
        dto.setProgramaId(ep.programaId);
        dto.setContactoEmergenciaNombre(ep.contactoEmergenciaNombre);
        dto.setContactoEmergenciaTelefono(ep.contactoEmergenciaTelefono);
        dto.setDireccion(ep.direccion);
        detalle.setPerfilEstudiante(dto);
      });
    }
    if (esAdministrativo) {
      administrativoPerfilRepository.buscarPorUsuarioId(usuario.id).ifPresent(ap -> {
        PerfilAdministrativo dto = new PerfilAdministrativo();
        dto.setCargoId(ap.cargo.id);
        dto.setCargoNombre(ap.cargo.nombre);
        dto.setAreaAdministrativa(ap.areaAdministrativa);
        dto.setCondicion(ap.condicion);
        detalle.setPerfilAdministrativo(dto);
      });
    }
    return detalle;
  }
}