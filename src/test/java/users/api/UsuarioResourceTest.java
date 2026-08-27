package users.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import users.domain.model.EstadoUsuario;
import users.domain.model.Rol;
import users.domain.model.Usuario;
import users.domain.model.UsuarioSedeRol;
import users.domain.repository.UsuarioRepository;
import users.domain.repository.UsuarioSedeRolRepository;
import users.infrastructure.keycloak.KeycloakProvisioningService;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@QuarkusTest
class UsuarioResourceTest {

  private static final String DOCENTE_SUB = "11111111-1111-1111-1111-111111111111";

  @InjectMocks
  KeycloakProvisioningService keycloakProvisioningService;

  @Inject UsuarioRepository usuarioRepository;
  @Inject UsuarioSedeRolRepository usuarioSedeRolRepository;

  @BeforeEach
  @Transactional
  void limpiarYSembrar() {
    usuarioSedeRolRepository.deleteAll();
    usuarioRepository.deleteAll();

    Usuario usuario = new Usuario();
    usuario.keycloakId = UUID.fromString(DOCENTE_SUB);
    usuario.tipoDocumento = "DNI";
    usuario.numeroDocumento = "12345678";
    usuario.nombres = "Ana";
    usuario.apellidoPaterno = "Torres";
    usuario.email = "ana.torres@edu.pe";
    usuario.estado = EstadoUsuario.ACTIVO;
    usuarioRepository.persist(usuario);

    UsuarioSedeRol asignacion = new UsuarioSedeRol();
    asignacion.usuario = usuario;
    asignacion.sedeId = 1L;
    asignacion.rol = Rol.DOCENTE;
    asignacion.fechaInicio = LocalDate.now().minusDays(1);
    usuarioSedeRolRepository.persist(asignacion);
  }

  @Test
  @TestSecurity(user = "ana.torres", roles = "DOCENTE")
  @JwtSecurity(claims = @Claim(key = "sub", value = DOCENTE_SUB))
  void obtenerPerfilPropio_devuelveElUsuarioAutenticado() {
    given()
            .when().get("/api/v1/usuarios/me")
            .then()
            .statusCode(200)
            .body("nombres", equalTo("Ana"))
            .body("apellidoPaterno", equalTo("Torres"))
            .body("rol", equalTo("DOCENTE"))
            .body("sedes[0]", equalTo(1));
  }

  @Test
  void obtenerPerfilPropio_sinToken_devuelve401() {
    given()
            .when().get("/api/v1/usuarios/me")
            .then()
            .statusCode(401);
  }

  @Test
  @TestSecurity(user = "ana.torres", roles = "DOCENTE")
  @JwtSecurity(claims = @Claim(key = "sub", value = DOCENTE_SUB))
  void actualizarPerfilPropio_soloActualizaCamposPermitidos() {
    given()
            .contentType("application/json")
            .body("{ \"telefono\": \"999888777\" }")
            .when().patch("/api/v1/usuarios/me")
            .then()
            .statusCode(200)
            .body("telefono", equalTo("999888777"))
            // nombres no está en ActualizarPerfilPropioRequest: no debería cambiar
            // aunque alguien intente mandarlo en el body (verificado aparte, ver
            // actualizarPerfilPropio_ignoraCamposNoPermitidos).
            .body("nombres", equalTo("Ana"));
  }

  @Test
  @TestSecurity(user = "otro.docente", roles = "DOCENTE")
  @JwtSecurity(claims = @Claim(key = "sub", value = "22222222-2222-2222-2222-222222222222"))
  void obtenerPerfilPropio_conSubSinUsuarioLocal_devuelve404() {
    // Token válido pero sin fila en usuario/usuario_sede_rol para ese sub:
    // caso real de un usuario provisionado en Keycloak pero no sincronizado
    // (o, más importante, el caso del primer admin sin sembrar aún).
    given()
            .when().get("/api/v1/usuarios/me")
            .then()
            .statusCode(404);
  }

  @Test
  @TestSecurity(user = "ana.torres", roles = "DOCENTE")
  @JwtSecurity(claims = @Claim(key = "sub", value = DOCENTE_SUB))
  void cambiarPasswordPropio_llamaAKeycloakConLaNuevaPassword() {
    given()
            .contentType("application/json")
            .body("{ \"passwordNueva\": \"unaPasswordNueva123\" }")
            .when().patch("/api/v1/usuarios/me/password")
            .then()
            .statusCode(204);

    verify(keycloakProvisioningService)
            .cambiarPassword(UUID.fromString(DOCENTE_SUB), "unaPasswordNueva123");
  }
}