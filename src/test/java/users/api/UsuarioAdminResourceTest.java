package users.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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

@QuarkusTest
class UsuarioAdminResourceTest {

  // Actor admin global (sedeId = null) usado como autor de las operaciones.
  private static final String ADMIN_SUB = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
  // Actor docente, solo para probar que un no-admin no puede entrar a estos endpoints.
  private static final String DOCENTE_SUB = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";

  @InjectMocks
  KeycloakProvisioningService keycloakProvisioningService;

  @Inject UsuarioRepository usuarioRepository;
  @Inject UsuarioSedeRolRepository usuarioSedeRolRepository;

  @BeforeEach
  @Transactional
  void limpiarYSembrarActores() {
    usuarioSedeRolRepository.deleteAll();
    usuarioRepository.deleteAll();

    sembrarActor(ADMIN_SUB, "Admin", "Global", "admin.global@edu.pe", Rol.ADMIN, null);
    sembrarActor(DOCENTE_SUB, "Docente", "Cualquiera", "docente@edu.pe", Rol.DOCENTE, 1L);
  }

  private void sembrarActor(String sub, String nombres, String apellido, String email, Rol rol, Long sedeId) {
    Usuario usuario = new Usuario();
    usuario.keycloakId = UUID.fromString(sub);
    usuario.tipoDocumento = "DNI";
    usuario.numeroDocumento = sub.substring(0, 8);
    usuario.nombres = nombres;
    usuario.apellidoPaterno = apellido;
    usuario.email = email;
    usuario.estado = EstadoUsuario.ACTIVO;
    usuarioRepository.persist(usuario);

    UsuarioSedeRol asignacion = new UsuarioSedeRol();
    asignacion.usuario = usuario;
    asignacion.sedeId = sedeId;
    asignacion.rol = rol;
    asignacion.fechaInicio = LocalDate.now().minusDays(1);
    usuarioSedeRolRepository.persist(asignacion);
  }

  @Test
  @TestSecurity(user = "admin.global", roles = "ADMIN")
  @JwtSecurity(claims = @Claim(key = "sub", value = ADMIN_SUB))
  void crearUsuario_conRolAdmin_creaEnBdYAsignaRolEnKeycloak() {
    when(keycloakProvisioningService.crearUsuario(
            "87654321", "carlos.rios@edu.pe", "Carlos", "Ríos", Rol.DOCENTE))
            .thenReturn(UUID.randomUUID());

    given()
            .contentType("application/json")
            .body("""
                {
                  "tipoDocumento": "DNI",
                  "numeroDocumento": "87654321",
                  "nombres": "Carlos",
                  "apellidoPaterno": "Ríos",
                  "email": "carlos.rios@edu.pe",
                  "rol": "DOCENTE",
                  "sedeId": 1
                }
                """)
            .when().post("/api/v1/usuarios")
            .then()
            .statusCode(201)
            .body("nombres", equalTo("Carlos"))
            .body("rol", equalTo("DOCENTE"));
  }

  @Test
  @TestSecurity(user = "docente.cualquiera", roles = "DOCENTE")
  @JwtSecurity(claims = @Claim(key = "sub", value = DOCENTE_SUB))
  void listarUsuarios_conRolDocente_devuelve403() {
    given()
            .when().get("/api/v1/usuarios")
            .then()
            .statusCode(403);
  }

  @Test
  void listarUsuarios_sinToken_devuelve401() {
    given()
            .when().get("/api/v1/usuarios")
            .then()
            .statusCode(401);
  }

  @Test
  @TestSecurity(user = "admin.global", roles = "ADMIN")
  @JwtSecurity(claims = @Claim(key = "sub", value = ADMIN_SUB))
  void crearUsuario_conDocumentoDuplicado_devuelve409() {
    when(keycloakProvisioningService.crearUsuario(
            anyString(), anyString(), anyString(), anyString(), any()))
            .thenReturn(UUID.randomUUID());

    String body = """
            {
              "tipoDocumento": "DNI",
              "numeroDocumento": "11223344",
              "nombres": "Luisa",
              "apellidoPaterno": "Vega",
              "email": "luisa.vega@edu.pe",
              "rol": "ESTUDIANTE",
              "sedeId": 1
            }
            """;

    given().contentType("application/json").body(body)
            .when().post("/api/v1/usuarios")
            .then().statusCode(201);

    given().contentType("application/json").body(body)
            .when().post("/api/v1/usuarios")
            .then().statusCode(409);
  }

  @Test
  @TestSecurity(user = "admin.global", roles = "ADMIN")
  @JwtSecurity(claims = @Claim(key = "sub", value = ADMIN_SUB))
  void obtenerUsuario_inexistente_devuelve404() {
    given()
            .when().get("/api/v1/usuarios/999999")
            .then()
            .statusCode(404);
  }
}