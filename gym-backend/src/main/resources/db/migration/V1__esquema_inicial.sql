CREATE TABLE planes (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(60) NOT NULL UNIQUE,
    precio INTEGER NOT NULL CHECK (precio > 0),
    hora_inicio TIME,
    hora_fin TIME,
    solo_menores_escolares BOOLEAN NOT NULL DEFAULT FALSE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_planes_horario CHECK (
        (hora_inicio IS NULL AND hora_fin IS NULL)
        OR (hora_inicio IS NOT NULL AND hora_fin IS NOT NULL AND hora_inicio < hora_fin)
    )
);

CREATE TABLE usuarios (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    rol VARCHAR(20) NOT NULL DEFAULT 'ADMIN' CHECK (rol IN ('ADMIN')),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE socios (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombres VARCHAR(80) NOT NULL,
    apellidos VARCHAR(80) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    es_escolar BOOLEAN NOT NULL DEFAULT FALSE,
    plan_id BIGINT NOT NULL REFERENCES planes (id),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_socios_plan ON socios (plan_id);

CREATE TABLE mensualidades (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    socio_id BIGINT NOT NULL REFERENCES socios (id),
    plan_id BIGINT NOT NULL REFERENCES planes (id),
    monto INTEGER NOT NULL CHECK (monto > 0),
    fecha_inicio DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    estado VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE'
        CHECK (estado IN ('PENDIENTE', 'PARCIAL', 'PAGADA', 'ANULADA')),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_mensualidades_fechas CHECK (fecha_vencimiento > fecha_inicio),
    CONSTRAINT ux_mensualidades_socio_inicio UNIQUE (socio_id, fecha_inicio)
);
CREATE INDEX ix_mensualidades_vencimiento ON mensualidades (fecha_vencimiento);

CREATE TABLE autorizaciones_abono (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    mensualidad_id BIGINT NOT NULL REFERENCES mensualidades (id),
    motivo VARCHAR(300) NOT NULL,
    solicitada_por BIGINT NOT NULL REFERENCES usuarios (id),
    estado VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE'
        CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA', 'USADA')),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_autorizaciones_mensualidad ON autorizaciones_abono (mensualidad_id);

CREATE TABLE aprobaciones_abono (
    autorizacion_id BIGINT NOT NULL REFERENCES autorizaciones_abono (id),
    usuario_id BIGINT NOT NULL REFERENCES usuarios (id),
    decision VARCHAR(15) NOT NULL CHECK (decision IN ('APROBADA', 'RECHAZADA')),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (autorizacion_id, usuario_id)
);

CREATE TABLE pagos (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    mensualidad_id BIGINT NOT NULL REFERENCES mensualidades (id),
    monto INTEGER NOT NULL CHECK (monto > 0),
    metodo VARCHAR(15) NOT NULL CHECK (metodo IN ('EFECTIVO', 'TRANSFERENCIA')),
    referencia VARCHAR(80),
    fecha_pago TIMESTAMPTZ NOT NULL DEFAULT now(),
    registrado_por BIGINT NOT NULL REFERENCES usuarios (id),
    autorizacion_id BIGINT REFERENCES autorizaciones_abono (id),
    clave_idempotencia VARCHAR(64) NOT NULL UNIQUE,
    anulado BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT ck_pagos_referencia CHECK (metodo <> 'TRANSFERENCIA' OR referencia IS NOT NULL)
);
CREATE INDEX ix_pagos_mensualidad ON pagos (mensualidad_id);
CREATE UNIQUE INDEX ux_pagos_transferencia_ref ON pagos (referencia)
    WHERE metodo = 'TRANSFERENCIA' AND NOT anulado;

INSERT INTO planes (nombre, precio, hora_inicio, hora_fin, solo_menores_escolares) VALUES
    ('Normal', 30000, NULL, NULL, FALSE),
    ('Escolar', 20000, '08:00', '19:00', TRUE);