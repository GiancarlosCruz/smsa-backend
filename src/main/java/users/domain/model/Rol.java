package users.domain.model;

/**
 * Debe mantenerse en sincronía con los realm roles definidos en Keycloak
 * (admin, docente, estudiante). El valor que llega en el claim realm_access.roles
 * del JWT se mapea 1 a 1 contra este enum al resolver el ContextoAcceso.
 */
public enum Rol {
  ADMIN,
  DOCENTE,
  ESTUDIANTE
}