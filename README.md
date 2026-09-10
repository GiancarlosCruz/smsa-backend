# system-smsa

Backend para gestión de usuarios, perfiles y permisos por sede en instituciones educativas.

---

## 1. Resumen Ejecutivo

Módulo centralizado de identidad y perfiles académicos basado en Java 21 y Quarkus. Centraliza la identidad mediante Keycloak (OIDC), mantiene un registro local de usuarios y asignaciones por sede/rol, y expone APIs REST para autoservicio y administración.

### Stack Tecnológico
* **Lenguaje:** Java 21
* **Framework:** Quarkus 3.15.3 (Resource Server OIDC, RESTEasy Reactive/Jackson, Hibernate Validator)
* **Persistencia:** PostgreSQL, Hibernate ORM con Panache, Flyway
* **Seguridad e Identidad:** Keycloak (OIDC Bearer Tokens + Admin REST API Client)
* **Documentación & Métricas:** OpenAPI / Swagger UI, Micrometer, Prometheus, SmallRye Health

### Estado del Proyecto
* **Nivel de madurez:** MVP Funcional (75% - 85%)
* **Implementado:** Estructura base, flujo de autenticación/autorización OIDC, persistencia, aprovisionamiento en Keycloak y suite de pruebas iniciales.
* **Pendientes críticos:** Corrección de la migración inicial de base de datos, validaciones de dominio y refinamiento de reglas de negocio previas a producción.

---

## 2. Arquitectura y Estructura

Enfoque monolítico modular por capas dentro del paquete raíz `users`, manteniendo el dominio desacoplado de dependencias de infraestructura.

### Estructura del Código Fuente
```text
src/
├── main/
│   ├── java/users/
│   │   ├── api/                 # Recursos JAX-RS / Endpoints REST
│   │   │   ├── UsuarioResource.java
│   │   │   └── UsuarioAdminResource.java
│   │   ├── application/         # Servicios de aplicación y casos de uso
│   │   │   ├── UsuarioService.java
│   │   │   ├── PerfilPropioService.java
│   │   │   └── RolSedeService.java
│   │   ├── domain/              # Lógica de dominio puro y repositorios
│   │   │   ├── exception/
│   │   │   ├── model/           # Entidades JPA (Usuario, Perfiles, Enums)
│   │   │   │   ├── Usuario.java
│   │   │   │   ├── UsuarioSedeRol.java
│   │   │   │   ├── DocentePerfil.java
│   │   │   │   ├── EstudiantePerfil.java
│   │   │   │   ├── Rol.java
│   │   │   │   └── EstadoUsuario.java
│   │   │   └── repository/      # PanacheRepositoryBase
│   │   │       ├── UsuarioRepository.java
│   │   │       ├── UsuarioSedeRolRepository.java
│   │   │       ├── DocentePerfilRepository.java
│   │   │       └── EstudiantePerfilRepository.java
│   │   ├── infrastructure/      # Clientes e integraciones externas
│   │   │   └── keycloak/
│   │   │       ├── KeycloakProvisioningService.java
│   │   │       └── KeycloakSyncException.java
│   │   ├── security/            # Contexto de seguridad y mappers
│   │   │   ├── ContextoAcceso.java
│   │   │   └── ContextoAccesoProvider.java
│   │   └── config/              # Clases de configuración del módulo
│   │       └── UsuarioModuleConfig.java
│   ├── openapi/
│   │   └── usuarios-perfil-openapi.yaml
│   └── resources/
│       ├── application.properties
│       └── db.migration/
│           └── V1__crear_modulo_usuarios.sql
└── test/java/users/api/         # Pruebas funcionales de endpoints
    ├── UsuarioResourceTest.java
    └── UsuarioAdminResourceTest.java

```text
##Flujo de Datos Principal
+-------------------+       +-------------------+       +-------------------+