-- V1__crear_modulo_usuarios.sql
-- Módulo Usuarios y Perfiles

CREATE TABLE usuario (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    keycloak_id          UUID         NOT NULL,
    tipo_documento       VARCHAR(20)  NOT NULL,
    numero_documento     VARCHAR(20)  NOT NULL,
    nombres              VARCHAR(150) NOT NULL,
    apellido_paterno     VARCHAR(100) NOT NULL,
    apellido_materno     VARCHAR(100),
    email                VARCHAR(150) NOT NULL,
    telefono             VARCHAR(20),
    estado               VARCHAR(20)  NOT NULL,
    fecha_creacion       TIMESTAMP    NOT NULL DEFAULT now(),
    fecha_actualizacion  TIMESTAMP,
    creado_por           VARCHAR(100),
    actualizado_por      VARCHAR(100),

    CONSTRAINT uk_usuario_keycloak_id UNIQUE (keycloak_id),
    CONSTRAINT uk_usuario_documento UNIQUE (tipo_documento, numero_documento),
    CONSTRAINT ck_usuario_estado CHECK (estado IN ('ACTIVO', 'INACTIVO', 'SUSPENDIDO'))
);

CREATE TABLE usuario_sede_rol (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id    BIGINT      NOT NULL REFERENCES usuario (id),
    sede_id       BIGINT,          -- NULL = acceso global; solo válido para rol ADMIN (regla de negocio, no de esquema)
    rol           VARCHAR(20) NOT NULL CHECK (rol IN ('ADMIN','DOCENTE','ESTUDIANTE')),
    fecha_inicio  DATE        NOT NULL,
    fecha_fin     DATE,

    CONSTRAINT ck_usuario_sede_rol_rol CHECK (rol IN ('ADMIN', 'DOCENTE', 'ESTUDIANTE')),
    CONSTRAINT ck_usuario_sede_rol_fechas CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
);

CREATE INDEX ix_usuario_sede_rol_usuario ON usuario_sede_rol (usuario_id);
CREATE INDEX ix_usuario_sede_rol_sede ON usuario_sede_rol (sede_id) WHERE sede_id IS NOT NULL;
-- Cubre la query de ContextoAccesoProvider (vigentesDe): filtra por usuario y por vigencia.
CREATE INDEX ix_usuario_sede_rol_vigencia ON usuario_sede_rol (usuario_id, fecha_fin);

CREATE TABLE docente_perfil (
    usuario_id      BIGINT PRIMARY KEY REFERENCES usuario (id),
    grado_academico VARCHAR(100),
    especialidad    VARCHAR(150),
    tipo_contrato   VARCHAR(50),
    fecha_ingreso   DATE NOT NULL
    CONSTRAINT fk_docente_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE TABLE estudiante_perfil (
    usuario_id                     BIGINT PRIMARY KEY REFERENCES usuario (id),
    codigo_estudiante              VARCHAR(20) NOT NULL,
    programa_id                    BIGINT,      -- referencia por id al módulo académico, sin FK física entre módulos
    contacto_emergencia_nombre     VARCHAR(150),
    contacto_emergencia_telefono   VARCHAR(20),
    direccion                      VARCHAR(250),

    CONSTRAINT uk_estudiante_codigo UNIQUE (codigo_estudiante)
    CONSTRAINT fk_estudiante_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);