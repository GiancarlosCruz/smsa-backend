-- V2__agregar_campos_legado.sql

ALTER TABLE usuario ADD COLUMN sexo VARCHAR(1);
ALTER TABLE usuario ADD CONSTRAINT ck_usuario_sexo CHECK (sexo IN ('M', 'F'));
ALTER TABLE usuario ADD COLUMN fecha_nacimiento DATE;

ALTER TABLE docente_perfil ADD COLUMN direccion VARCHAR(250);
ALTER TABLE docente_perfil ADD COLUMN cargo_administrativo VARCHAR(150);

ALTER TABLE estudiante_perfil ADD COLUMN periodo_ingreso VARCHAR(20);
ALTER TABLE estudiante_perfil ADD COLUMN tipo_registro VARCHAR(20);
ALTER TABLE estudiante_perfil ADD CONSTRAINT ck_estudiante_tipo_registro
    CHECK (tipo_registro IN ('INSCRIPCION', 'CONVALIDACION', 'TRASLADO', 'OTRO'));