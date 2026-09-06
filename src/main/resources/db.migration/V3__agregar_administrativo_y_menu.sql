-- V3__agregar_administrativo_y_menu.sql

-- El check de rol en usuario_sede_rol necesita aceptar ADMINISTRATIVO ahora.
ALTER TABLE usuario_sede_rol DROP CONSTRAINT usuario_sede_rol_rol_check;
ALTER TABLE usuario_sede_rol ADD CONSTRAINT usuario_sede_rol_rol_check
    CHECK (rol IN ('ADMIN', 'DOCENTE', 'ESTUDIANTE', 'ADMINISTRATIVO'));

CREATE TABLE cargo (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    descripcion VARCHAR(250),

    CONSTRAINT uk_cargo_nombre UNIQUE (nombre)
);

CREATE TABLE administrativo_perfil (
    usuario_id          BIGINT PRIMARY KEY REFERENCES usuario (id),
    cargo_id            BIGINT NOT NULL REFERENCES cargo (id),
    area_administrativa VARCHAR(150),
    condicion           VARCHAR(20)  -- CONTRATADO, NOMBRADO (texto libre, igual que tipo_contrato de docente)
);

CREATE INDEX ix_administrativo_perfil_cargo ON administrativo_perfil (cargo_id);

CREATE TABLE opcion_menu (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo          VARCHAR(50)  NOT NULL,
    etiqueta        VARCHAR(100) NOT NULL,
    icono           VARCHAR(50),
    ruta_frontend   VARCHAR(150),
    orden           INT NOT NULL DEFAULT 0,
    opcion_padre_id BIGINT REFERENCES opcion_menu (id),  -- para submenús, no usado en el MVP del endpoint

    CONSTRAINT uk_opcion_menu_codigo UNIQUE (codigo)
);

CREATE TABLE cargo_opcion_menu (
    cargo_id       BIGINT NOT NULL REFERENCES cargo (id),
    opcion_menu_id BIGINT NOT NULL REFERENCES opcion_menu (id),

    PRIMARY KEY (cargo_id, opcion_menu_id)
);