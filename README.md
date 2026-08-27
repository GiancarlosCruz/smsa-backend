# System SMSA

Backend para la gestión de **usuarios, perfiles, roles y permisos por sede** para una institución educativa.

El proyecto está construido sobre **Java 21 y Quarkus 3.15.3**, utilizando Keycloak como proveedor de identidad y autorización, PostgreSQL como base de datos y una arquitectura modular por capas.

---

## 📋 Descripción general

### Objetivo

Proporcionar una API REST para administrar:

* Usuarios.
* Perfiles de usuarios.
* Roles.
* Asignaciones de usuarios por sede.
* Estados de usuarios.
* Permisos de acceso.
* Integración y sincronización con Keycloak.

### Estado del proyecto

> **Estado:** MVP funcional
> **Madurez estimada:** 70–80%

El núcleo del módulo de usuarios se encuentra implementado y cuenta con una base técnica sólida. Aún existen pendientes relacionados principalmente con reglas de negocio, migraciones SQL, validaciones y cobertura de pruebas.

---

## 🛠️ Tecnologías

| Tecnología        | Versión / Uso                 |
| ----------------- | ----------------------------- |
| Java              | 21                            |
| Quarkus           | 3.15.3                        |
| Hibernate ORM     | Persistencia JPA              |
| Panache           | Repositorios y acceso a datos |
| PostgreSQL        | Base de datos                 |
| Flyway            | Migraciones                   |
| Keycloak          | Autenticación y autorización  |
| OIDC              | Seguridad basada en JWT       |
| RESTEasy Jackson  | API REST / JSON               |
| JAX-RS            | Endpoints REST                |
| Bean Validation   | Validación de datos           |
| OpenAPI / Swagger | Documentación de API          |
| MapStruct         | Mapeo de objetos              |
| Micrometer        | Métricas                      |
| Prometheus        | Monitorización                |
| SmallRye Health   | Health checks                 |

---

# 🏗️ Arquitectura

El proyecto utiliza una arquitectura por capas con **modularización funcional**, siguiendo un enfoque de **Modular Monolith**.

No se trata de una arquitectura de microservicios. El dominio relacionado con usuarios se encuentra concentrado dentro del módulo `users`.

### Capas principales

```text
API
 ↓
Application
 ↓
Domain
 ↓
Infrastructure
 ↓
Security
```

### Estructura

```text
src/
├── main/
│   ├── java/
│   │   └── users/
│   │       ├── api/
│   │       │   └── Recursos REST
│   │       │
│   │       ├── application/
│   │       │   ├── UsuarioService
│   │       │   ├── PerfilPropioService
│   │       │   └── RolSedeService
│   │       │
│   │       ├── domain/
│   │       │   ├── model/
│   │       │   ├── repository/
│   │       │   └── exception/
│   │       │
│   │       ├── infrastructure/
│   │       │   └── keycloak/
│   │       │
│   │       ├── security/
│   │       │   ├── providers/
│   │       │   └── contexto de acceso
│   │       │
│   │       └── config/
│   │
│   └── resources/
│       ├── application.properties
│       └── db/
│           └── migration/
│               └── V1__crear_modulo_usuarios.sql
│
├── openapi/
│   └── usuarios-perfil-openapi.yaml
│
└── test/
    └── java/
        └── users/
            └── api/
```

---

# 🔐 Flujo de autenticación y autorización

El flujo principal de una petición es:

```text
Cliente
   │
   │ JWT
   ▼
Keycloak
   │
   │ OIDC
   ▼
Quarkus
   │
   ▼
ContextoAccesoProvider
   │
   ├── Identifica usuario mediante `sub`
   │
   └── Consulta asignaciones UsuarioSedeRol
   │
   ▼
REST Resource
   │
   ▼
Application Service
   │
   ▼
Repository
   │
   ▼
PostgreSQL
```

En operaciones de creación o actualización también interviene Keycloak Admin API:

```text
REST API
   │
   ▼
UsuarioService
   │
   ├──────────────► PostgreSQL
   │
   └──────────────► Keycloak Admin API
```

La base de datos local mantiene la información de identidad, estado y permisos asociados a sedes y roles.

---

# 🚀 Funcionalidades

| Módulo                     | Componente principal                                  | Estado       |
| -------------------------- | ----------------------------------------------------- | ------------ |
| Perfil propio              | `UsuarioResource.java`                                | ✅ Completo   |
| Administración de usuarios | `UsuarioAdminResource.java`                           | ✅ Funcional  |
| Gestión de usuarios        | `UsuarioService.java`                                 | ✅ Completo   |
| Perfil propio              | `PerfilPropioService.java`                            | ✅ Completo   |
| Asignación rol/sede        | `RolSedeService.java`                                 | ⚠️ Parcial   |
| Contexto de acceso         | `ContextoAcceso.java` / `ContextoAccesoProvider.java` | ✅ Completo   |
| Entidades de dominio       | `domain/model`                                        | ✅ Completo   |
| Repositorios               | `domain/repository`                                   | ✅ Funcional  |
| Integración Keycloak       | `KeycloakProvisioningService.java`                    | ✅ Completo   |
| Migración de BD            | `V1__crear_modulo_usuarios.sql`                       | ⚠️ Pendiente |
| OpenAPI                    | `usuarios-perfil-openapi.yaml`                        | ✅ Completo   |
| Tests REST                 | `src/test/java/users/api`                             | ⚠️ Parcial   |

---

# 🌐 API REST

La API utiliza como prefijo:

```text
/api/v1
```

## Perfil propio

### Obtener perfil

```http
GET /api/v1/usuarios/me
```

Obtiene la información del usuario autenticado.

**Autenticación:** JWT requerido.

### Actualizar perfil

```http
PATCH /api/v1/usuarios/me
```

Permite actualizar los campos editables del propio usuario, incluyendo información de contacto y datos relacionados con el perfil de estudiante.

---

## Administración de usuarios

### Listar usuarios

```http
GET /api/v1/usuarios
```

Permite obtener un listado paginado de usuarios.

Filtros disponibles:

```text
sedeId
rol
estado
q
page
size
```

### Crear usuario

```http
POST /api/v1/usuarios
```

Crea un nuevo usuario y realiza la correspondiente provisión en Keycloak.

### Obtener usuario

```http
GET /api/v1/usuarios/{id}
```

Obtiene el detalle de un usuario.

### Actualizar usuario

```http
PUT /api/v1/usuarios/{id}
```

Actualiza información administrativa del usuario.

### Cambiar estado

```http
PATCH /api/v1/usuarios/{id}/estado
```

Permite activar, suspender o inactivar un usuario.

### Asignar rol y sede

```http
POST /api/v1/usuarios/{id}/roles
```

Asigna un rol y una sede al usuario.

---

# 🔑 Autorización

La seguridad está basada en:

* JWT.
* OpenID Connect.
* Keycloak.
* Realm Roles.
* Asignaciones locales de usuario, sede y rol.

Los roles se obtienen desde:

```text
realm_access.roles
```

Sin embargo, el acceso específico por sede no depende exclusivamente del JWT.

La fuente local de verdad es:

```text
UsuarioSedeRol
```

Esto permite determinar las sedes y roles vigentes de cada usuario.

---

# 🧩 Modelos principales

## Usuario

Representa la información principal del usuario.

```text
id
keycloakId
tipoDocumento
numeroDocumento
nombres
apellidoPaterno
apellidoMaterno
email
telefono
estado
```

## UsuarioSedeRol

Representa la asignación de un usuario a una sede y un rol.

```text
usuario
sedeId
rol
fechaInicio
fechaFin
```

Esta entidad constituye la fuente principal para determinar los permisos asociados a una sede.

## DocentePerfil

```text
gradoAcademico
especialidad
tipoContrato
fechaIngreso
```

## EstudiantePerfil

```text
codigoEstudiante
programaId
contactoEmergenciaNombre
contactoEmergenciaTelefono
direccion
```

## Roles

```text
ADMIN
DOCENTE
ESTUDIANTE
```

## Estados

```text
ACTIVO
INACTIVO
SUSPENDIDO
```

---

# 🗄️ Persistencia

La persistencia se implementa mediante:

* Hibernate ORM.
* Panache.
* PostgreSQL.
* Flyway.

Las migraciones se encuentran en:

```text
src/main/resources/db/migration/
```

Migración inicial:

```text
V1__crear_modulo_usuarios.sql
```

---

# 🔄 Integración con Keycloak

La integración con Keycloak se encuentra encapsulada dentro de:

```text
users/infrastructure/keycloak/
```

Componente principal:

```text
KeycloakProvisioningService.java
```

Responsabilidades principales:

* Crear usuarios en Keycloak.
* Actualizar información.
* Deshabilitar usuarios.
* Gestionar roles.
* Sincronizar identidad entre Keycloak y la base de datos local.

Esta separación permite mantener la lógica específica de Keycloak fuera del dominio principal de la aplicación.

---

# 🧪 Pruebas

Las pruebas REST se encuentran en:

```text
src/test/java/users/api/
```

Actualmente existe cobertura para:

* Autenticación.
* Autorización.
* CRUD de usuarios.
* Permisos.
* Acceso a endpoints.

### Pendientes de cobertura

Se recomienda ampliar las pruebas para cubrir:

* Usuarios con múltiples sedes.
* Usuarios con múltiples roles.
* Asignaciones vigentes.
* Asignaciones vencidas.
* Administrador global.
* Administrador de sede.
* Errores provenientes de Keycloak.
* Usuarios desincronizados.
* Validaciones de entrada.
* Migraciones de base de datos.

---

# ⚠️ Deuda técnica

## 1. Migración SQL

La migración:

```text
V1__crear_modulo_usuarios.sql
```

presenta errores de sintaxis relacionados con restricciones de las tablas:

```text
docente_perfil
estudiante_perfil
```

Se deben corregir antes de considerar la migración lista para despliegues reales.

**Prioridad: 🔴 Alta**

---

## 2. Conteo incorrecto en paginación

En:

```text
UsuarioAdminResource.listarUsuarios()
```

el total de registros utiliza un conteo global:

```text
usuarioRepository.count()
```

en lugar de un conteo que respete los filtros aplicados.

Esto puede provocar resultados incorrectos en la paginación.

**Prioridad: 🟠 Media/Alta**

---

## 3. Rol efectivo

Actualmente `ContextoAccesoProvider` obtiene el primer rol de las asignaciones vigentes.

Esto genera ambigüedad cuando un usuario posee:

* Varias sedes.
* Varios roles.
* Varias asignaciones vigentes.

Se requiere definir formalmente el concepto de **rol efectivo**.

**Prioridad: 🟠 Media/Alta**

---

## 4. Reglas de acceso por sede

Las reglas de negocio relacionadas con sede y rol están distribuidas principalmente entre:

```text
UsuarioService
RolSedeService
ContextoAccesoProvider
```

Se recomienda centralizar y reforzar estas reglas.

Debe definirse claramente la diferencia entre:

```text
ADMIN GLOBAL
ADMIN DE SEDE
```

**Prioridad: 🟠 Media/Alta**

---

## 5. `sedeId = null`

Actualmente:

```text
sedeId = null
```

puede representar acceso global.

Sin embargo, esta regla debería estar formalmente definida y validada tanto a nivel de dominio como de persistencia.

**Prioridad: 🟠 Media**

---

## 6. Sincronización con Keycloak

La aplicación contempla mecanismos de compensación cuando falla la sincronización.

Sin embargo, todavía no existe un proceso de reconciliación para detectar automáticamente:

```text
BD local ≠ Keycloak
```

Se recomienda implementar un mecanismo de reconciliación.

**Prioridad: 🟠 Media**

---

## 7. Validaciones

Se recomienda reforzar las validaciones mediante:

```java
@Valid
```

y Bean Validation para:

* Email.
* Documento.
* Longitudes.
* Campos obligatorios.
* Estados válidos.
* Asignaciones.
* Fechas de vigencia.
* Reglas específicas por tipo de usuario.

---

# 📌 Roadmap

### Alta prioridad

* [ ] Corregir migración `V1__crear_modulo_usuarios.sql`.
* [ ] Validar completamente el esquema PostgreSQL.
* [ ] Corregir el conteo de paginación con filtros.
* [ ] Definir las reglas de rol efectivo.
* [ ] Formalizar las reglas de acceso por sede.

### Media prioridad

* [ ] Ampliar pruebas de integración.
* [ ] Cubrir escenarios multi-sede.
* [ ] Cubrir múltiples roles vigentes.
* [ ] Cubrir asignaciones vencidas.
* [ ] Cubrir errores de Keycloak.
* [ ] Completar validaciones con Bean Validation.
* [ ] Implementar reconciliación Keycloak ↔ BD.

### Operación y producción

* [ ] Definir perfiles `dev`, `qa` y `prod`.
* [ ] Gestionar secretos de forma segura.
* [ ] Configurar observabilidad.
* [ ] Configurar métricas con Micrometer.
* [ ] Configurar Prometheus.
* [ ] Configurar health checks.
* [ ] Definir estrategia de despliegue.
* [ ] Documentar configuración de Keycloak por ambiente.

---

# 📊 Estado general

| Área                        | Estado          |
| --------------------------- | --------------- |
| Arquitectura                | 🟢 Sólida       |
| API REST                    | 🟢 Funcional    |
| Gestión de usuarios         | 🟢 Funcional    |
| Perfil propio               | 🟢 Completo     |
| Keycloak                    | 🟢 Integrado    |
| Persistencia                | 🟢 Implementada |
| OpenAPI                     | 🟢 Completo     |
| Migraciones                 | 🔴 Pendiente    |
| Validaciones                | 🟠 En progreso  |
| Reglas por sede             | 🟠 En progreso  |
| Tests                       | 🟠 En progreso  |
| Reconciliación Keycloak     | 🔴 Pendiente    |
| Preparación para producción | 🟠 Pendiente    |

---

# 🎯 Conclusión

`system-smsa` cuenta con una base técnica sólida para la gestión de usuarios, perfiles y permisos.

La combinación de:

```text
Java 21
    +
Quarkus 3.15.3
    +
Hibernate ORM / Panache
    +
PostgreSQL
    +
Flyway
    +
Keycloak
    +
OpenAPI
```

proporciona una arquitectura adecuada para continuar evolucionando el sistema.

El núcleo funcional se encuentra implementado. Antes de considerar el proyecto **production-ready**, se deben resolver principalmente:

1. Migración SQL.
2. Reglas de negocio de roles y sedes.
3. Paginación con filtros.
4. Validaciones.
5. Cobertura de pruebas.
6. Reconciliación entre Keycloak y la BD local.
7. Configuración operativa para ambientes reales.

---

# 📝 Validación técnica

La documentación se elaboró a partir de la revisión de:

* Código fuente.
* Estructura del proyecto.
* Contrato OpenAPI.
* Migraciones SQL.
* Tests existentes.
* Configuración de Quarkus.

La ejecución automática de Maven en el entorno de análisis estuvo limitada por un problema de permisos del host. Por este motivo, parte de la evaluación se realizó mediante **análisis estático del código y de la estructura existente del proyecto**.

---

## 👥 Equipo

**System SMSA**
Backend de gestión de usuarios, perfiles, roles y permisos.

---
