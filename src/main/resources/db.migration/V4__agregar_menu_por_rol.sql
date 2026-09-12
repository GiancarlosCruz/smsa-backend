-- V4__agregar_menu_por_rol.sql

CREATE TABLE rol_opcion_menu (
    rol            VARCHAR(20) NOT NULL,
    opcion_menu_id BIGINT      NOT NULL REFERENCES opcion_menu (id),

    PRIMARY KEY (rol, opcion_menu_id),
    CONSTRAINT ck_rol_opcion_menu_rol CHECK (rol IN ('ADMIN', 'DOCENTE', 'ESTUDIANTE', 'ADMINISTRATIVO'))
);