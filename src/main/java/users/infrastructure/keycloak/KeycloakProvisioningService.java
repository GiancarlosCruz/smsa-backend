package users.infrastructure.keycloak;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import users.domain.model.Rol;

import java.util.List;
import java.util.UUID;

/**
 * Encapsula el Admin REST API de Keycloak. Vive en infrastructure/ porque es
 * un sistema externo, igual que cualquier otro cliente HTTP a un tercero:
 * UsuarioService no debe saber cómo se crea un usuario en Keycloak, solo que
 * existe la operación de provisión.
 */
@ApplicationScoped
public class KeycloakProvisioningService {

  private static final Logger LOG = Logger.getLogger(KeycloakProvisioningService.class);

  @Inject
  Keycloak keycloakAdminClient;

  @ConfigProperty(name = "quarkus.keycloak.admin-client.realm")
  String realmName;

  /**
   * Crea el usuario en Keycloak con el DNI como username Y como contraseña
   * inicial, marcada temporal (fuerza a Keycloak a exigir un cambio en el
   * primer login — necesario porque el DNI es un dato fácil de adivinar,
   * no es un secreto real). Después de ese primer cambio forzado, el
   * usuario puede volver a cambiar su contraseña cuando quiera desde la
   * propia web, vía cambiarPassword(...) más abajo.
   *
   * Devuelve el keycloakId generado. El llamador (UsuarioService) debe
   * persistir ese id en Usuario.keycloakId de inmediato, en la misma
   * operación: si esa escritura local falla después de este punto, debe
   * invocar eliminarPorId(...) para compensar y no dejar un usuario
   * huérfano en Keycloak.
   */
  public UUID crearUsuario(String numeroDocumento, String email, String nombres, String apellidoPaterno, Rol rol) {
    UserRepresentation representacion = new UserRepresentation();
    representacion.setUsername(numeroDocumento);
    representacion.setEmail(email);
    representacion.setFirstName(nombres);
    representacion.setLastName(apellidoPaterno);
    representacion.setEnabled(true);
    representacion.setEmailVerified(false);
    representacion.setRequiredActions(List.of("UPDATE_PASSWORD"));
    representacion.setCredentials(List.of(credencialInicial(numeroDocumento)));

    UsersResource usersResource = realm().users();
    try (Response respuesta = usersResource.create(representacion)) {
      if (respuesta.getStatus() != 201) {
        throw new KeycloakSyncException(
                "No se pudo crear el usuario en Keycloak, status=" + respuesta.getStatus());
      }
      String keycloakId = extraerIdDeLocation(respuesta.getLocation().getPath());
      asignarRol(keycloakId, rol);
      return UUID.fromString(keycloakId);
    } catch (KeycloakSyncException e) {
      throw e;
    } catch (Exception e) {
      throw new KeycloakSyncException("Error creando usuario en Keycloak", e);
    }
  }

  private CredentialRepresentation credencialInicial(String numeroDocumento) {
    CredentialRepresentation credencial = new CredentialRepresentation();
    credencial.setType(CredentialRepresentation.PASSWORD);
    credencial.setValue(numeroDocumento);
    credencial.setTemporary(true);
    return credencial;
  }

  /**
   * Self-service: el propio usuario cambia su contraseña desde la web,
   * en cualquier momento (no solo en el primer login forzado). temporary
   * = false porque esta ya no es una contraseña provisoria.
   */
  public void cambiarPassword(UUID keycloakId, String passwordNueva) {
    CredentialRepresentation credencial = new CredentialRepresentation();
    credencial.setType(CredentialRepresentation.PASSWORD);
    credencial.setValue(passwordNueva);
    credencial.setTemporary(false);
    try {
      realm().users().get(keycloakId.toString()).resetPassword(credencial);
    } catch (Exception e) {
      throw new KeycloakSyncException("Error actualizando la contraseña en Keycloak", e);
    }
  }

  /**
   * Compensación best-effort cuando la creación en Keycloak tuvo éxito pero
   * la escritura local falló. Si esto también falla, queda un usuario
   * huérfano que se resuelve con un job de reconciliación aparte —
   * no se reintenta en caliente dentro del mismo request.
   */
  public void eliminarPorId(UUID keycloakId) {
    try {
      realm().users().get(keycloakId.toString()).remove();
    } catch (Exception e) {
      LOG.errorf(e,
              "No se pudo revertir la creación en Keycloak del usuario %s. Requiere reconciliación manual.",
              keycloakId);
    }
  }

  public void actualizarDatosBasicos(UUID keycloakId, String nombres, String apellidoPaterno, String email) {
    UserResource recurso = realm().users().get(keycloakId.toString());
    UserRepresentation representacion = recurso.toRepresentation();
    representacion.setFirstName(nombres);
    representacion.setLastName(apellidoPaterno);
    representacion.setEmail(email);
    try {
      recurso.update(representacion);
    } catch (Exception e) {
      throw new KeycloakSyncException("Error actualizando usuario en Keycloak", e);
    }
  }

  public void cambiarHabilitado(UUID keycloakId, boolean habilitado) {
    UserResource recurso = realm().users().get(keycloakId.toString());
    UserRepresentation representacion = recurso.toRepresentation();
    representacion.setEnabled(habilitado);
    try {
      recurso.update(representacion);
    } catch (Exception e) {
      throw new KeycloakSyncException("Error actualizando estado en Keycloak", e);
    }
  }

  private void asignarRol(String keycloakId, Rol rol) {
    RoleRepresentation roleRepresentation =
            realm().roles().get(rol.name()).toRepresentation();
    realm().users().get(keycloakId).roles().realmLevel().add(List.of(roleRepresentation));
  }

  private RealmResource realm() {
    return keycloakAdminClient.realm(realmName);
  }

  private String extraerIdDeLocation(String path) {
    return path.substring(path.lastIndexOf('/') + 1);
  }
}