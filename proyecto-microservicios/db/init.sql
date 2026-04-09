-- ============================================
-- BASE DE DATOS: gestion_usuarios_servicios
-- Motor: PostgreSQL
-- ============================================

-- ============================================
-- ELIMINAR TABLAS SI EXISTEN
-- ============================================
DROP TABLE IF EXISTS notificacion CASCADE;
DROP TABLE IF EXISTS detalle_factura CASCADE;
DROP TABLE IF EXISTS factura CASCADE;
DROP TABLE IF EXISTS pago CASCADE;
DROP TABLE IF EXISTS usuario_servicio CASCADE;
DROP TABLE IF EXISTS suscripcion CASCADE;
DROP TABLE IF EXISTS metodo_pago CASCADE;
DROP TABLE IF EXISTS servicio CASCADE;
DROP TABLE IF EXISTS plan_mensual CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;

-- ============================================
-- TABLA USUARIO
-- ============================================
CREATE TABLE usuario (
    id_usuario           SERIAL PRIMARY KEY,
    rut                  VARCHAR(15) UNIQUE,
    nombres              VARCHAR(100) NOT NULL,
    apellido_paterno     VARCHAR(100) NOT NULL,
    apellido_materno     VARCHAR(100),
    correo               VARCHAR(150) NOT NULL UNIQUE,
    telefono             VARCHAR(20),
    direccion            VARCHAR(200),
    ciudad               VARCHAR(100),
    fecha_registro       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado_cuenta        VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    clave_hash           VARCHAR(255) NOT NULL,
    observacion          TEXT,
    CONSTRAINT chk_estado_cuenta
        CHECK (estado_cuenta IN ('ACTIVA', 'SUSPENDIDA', 'BLOQUEADA', 'ELIMINADA'))
);

-- ============================================
-- TABLA PLANES MENSUALES
-- ============================================
CREATE TABLE plan_mensual (
    id_plan              SERIAL PRIMARY KEY,
    nombre_plan          VARCHAR(100) NOT NULL UNIQUE,
    descripcion          TEXT,
    precio_mensual       NUMERIC(12,2) NOT NULL,
    limite_usuarios      INTEGER DEFAULT 1,
    activo               BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_precio_plan CHECK (precio_mensual >= 0),
    CONSTRAINT chk_limite_usuarios CHECK (limite_usuarios > 0)
);

-- ============================================
-- TABLA SERVICIOS CONTRATABLES
-- ============================================
CREATE TABLE servicio (
    id_servicio          SERIAL PRIMARY KEY,
    nombre_servicio      VARCHAR(120) NOT NULL UNIQUE,
    descripcion          TEXT,
    costo_mensual        NUMERIC(12,2) NOT NULL,
    activo               BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_costo_servicio CHECK (costo_mensual >= 0)
);

-- ============================================
-- TABLA METODOS DE PAGO
-- ============================================
CREATE TABLE metodo_pago (
    id_metodo_pago       SERIAL PRIMARY KEY,
    nombre_metodo        VARCHAR(50) NOT NULL UNIQUE,
    descripcion          VARCHAR(150),
    activo               BOOLEAN NOT NULL DEFAULT TRUE
);

-- ============================================
-- TABLA SUSCRIPCION DEL USUARIO
-- Un usuario puede tener una suscripción activa a un plan
-- ============================================
CREATE TABLE suscripcion (
    id_suscripcion       SERIAL PRIMARY KEY,
    id_usuario           INTEGER NOT NULL,
    id_plan              INTEGER NOT NULL,
    fecha_inicio         DATE NOT NULL,
    fecha_fin            DATE,
    estado_suscripcion   VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    renovacion_automatica BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_suscripcion_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),

    CONSTRAINT fk_suscripcion_plan
        FOREIGN KEY (id_plan) REFERENCES plan_mensual(id_plan),

    CONSTRAINT chk_estado_suscripcion
        CHECK (estado_suscripcion IN ('ACTIVA', 'VENCIDA', 'CANCELADA', 'SUSPENDIDA')),

    CONSTRAINT chk_fechas_suscripcion
        CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
);

-- ============================================
-- TABLA SERVICIOS CONTRATADOS POR USUARIO
-- ============================================
CREATE TABLE usuario_servicio (
    id_usuario_servicio  SERIAL PRIMARY KEY,
    id_usuario           INTEGER NOT NULL,
    id_servicio          INTEGER NOT NULL,
    fecha_contratacion   DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_termino        DATE,
    estado               VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    precio_pactado       NUMERIC(12,2) NOT NULL,

    CONSTRAINT fk_usuario_servicio_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),

    CONSTRAINT fk_usuario_servicio_servicio
        FOREIGN KEY (id_servicio) REFERENCES servicio(id_servicio),

    CONSTRAINT chk_estado_usuario_servicio
        CHECK (estado IN ('ACTIVO', 'SUSPENDIDO', 'CANCELADO')),

    CONSTRAINT chk_precio_pactado
        CHECK (precio_pactado >= 0),

    CONSTRAINT chk_fechas_usuario_servicio
        CHECK (fecha_termino IS NULL OR fecha_termino >= fecha_contratacion),

    CONSTRAINT uq_usuario_servicio_activo
        UNIQUE (id_usuario, id_servicio, fecha_contratacion)
);

-- ============================================
-- TABLA PAGOS
-- Registra pago mensual del plan y/o servicios
-- ============================================
CREATE TABLE pago (
    id_pago              SERIAL PRIMARY KEY,
    id_usuario           INTEGER NOT NULL,
    id_suscripcion       INTEGER,
    id_metodo_pago       INTEGER NOT NULL,
    fecha_pago           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    periodo_anio         INTEGER NOT NULL,
    periodo_mes          INTEGER NOT NULL,
    monto_total          NUMERIC(12,2) NOT NULL,
    estado_pago          VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    codigo_transaccion   VARCHAR(100),
    observacion          TEXT,

    CONSTRAINT fk_pago_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),

    CONSTRAINT fk_pago_suscripcion
        FOREIGN KEY (id_suscripcion) REFERENCES suscripcion(id_suscripcion),

    CONSTRAINT fk_pago_metodo
        FOREIGN KEY (id_metodo_pago) REFERENCES metodo_pago(id_metodo_pago),

    CONSTRAINT chk_periodo_mes CHECK (periodo_mes BETWEEN 1 AND 12),
    CONSTRAINT chk_periodo_anio CHECK (periodo_anio >= 2020),
    CONSTRAINT chk_monto_total CHECK (monto_total >= 0),

    CONSTRAINT chk_estado_pago
        CHECK (estado_pago IN ('PENDIENTE', 'PAGADO', 'RECHAZADO', 'ANULADO'))
);

-- ============================================
-- TABLA FACTURA
-- ============================================
CREATE TABLE factura (
    id_factura           SERIAL PRIMARY KEY,
    id_pago              INTEGER NOT NULL,
    numero_factura       VARCHAR(50) NOT NULL UNIQUE,
    fecha_emision        DATE NOT NULL DEFAULT CURRENT_DATE,
    subtotal             NUMERIC(12,2) NOT NULL,
    impuesto             NUMERIC(12,2) NOT NULL DEFAULT 0,
    total                NUMERIC(12,2) NOT NULL,
    estado_factura       VARCHAR(20) NOT NULL DEFAULT 'EMITIDA',

    CONSTRAINT fk_factura_pago
        FOREIGN KEY (id_pago) REFERENCES pago(id_pago),

    CONSTRAINT chk_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_impuesto CHECK (impuesto >= 0),
    CONSTRAINT chk_total CHECK (total >= 0),

    CONSTRAINT chk_estado_factura
        CHECK (estado_factura IN ('EMITIDA', 'PAGADA', 'ANULADA'))
);

-- ============================================
-- TABLA DETALLE FACTURA
-- ============================================
CREATE TABLE detalle_factura (
    id_detalle_factura   SERIAL PRIMARY KEY,
    id_factura           INTEGER NOT NULL,
    tipo_item            VARCHAR(20) NOT NULL,
    descripcion_item     VARCHAR(200) NOT NULL,
    cantidad             INTEGER NOT NULL DEFAULT 1,
    precio_unitario      NUMERIC(12,2) NOT NULL,
    subtotal_item        NUMERIC(12,2) NOT NULL,

    CONSTRAINT fk_detalle_factura
        FOREIGN KEY (id_factura) REFERENCES factura(id_factura),

    CONSTRAINT chk_tipo_item
        CHECK (tipo_item IN ('PLAN', 'SERVICIO')),

    CONSTRAINT chk_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_precio_unitario CHECK (precio_unitario >= 0),
    CONSTRAINT chk_subtotal_item CHECK (subtotal_item >= 0)
);

-- ============================================
-- TABLA NOTIFICACIONES
-- ============================================
CREATE TABLE notificacion (
    id_notificacion      SERIAL PRIMARY KEY,
    id_usuario           INTEGER NOT NULL,
    tipo_notificacion    VARCHAR(20) NOT NULL,
    destino              VARCHAR(200) NOT NULL,
    asunto               VARCHAR(200),
    mensaje              TEXT NOT NULL,
    estado_envio         VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_envio          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacion          TEXT,

    CONSTRAINT fk_notificacion_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),

    CONSTRAINT chk_tipo_notificacion
        CHECK (tipo_notificacion IN ('EMAIL', 'SMS', 'INTERNA')),

    CONSTRAINT chk_estado_envio
        CHECK (estado_envio IN ('PENDIENTE', 'ENVIADA', 'ERROR'))
);

-- ============================================
-- ÍNDICES
-- ============================================
CREATE INDEX idx_usuario_correo ON usuario(correo);
CREATE INDEX idx_usuario_rut ON usuario(rut);
CREATE INDEX idx_suscripcion_usuario ON suscripcion(id_usuario);
CREATE INDEX idx_pago_usuario ON pago(id_usuario);
CREATE INDEX idx_pago_periodo ON pago(periodo_anio, periodo_mes);
CREATE INDEX idx_usuario_servicio_usuario ON usuario_servicio(id_usuario);
CREATE INDEX idx_notificacion_usuario ON notificacion(id_usuario);

-- ============================================
-- DATOS INICIALES
-- ============================================

-- Métodos de pago
INSERT INTO metodo_pago (nombre_metodo, descripcion) VALUES
('TARJETA', 'Pago con tarjeta de crédito o débito'),
('TRANSFERENCIA', 'Pago por transferencia bancaria'),
('EFECTIVO', 'Pago presencial en efectivo');

-- Planes mensuales
INSERT INTO plan_mensual (nombre_plan, descripcion, precio_mensual, limite_usuarios) VALUES
('BÁSICO', 'Plan básico mensual', 9990, 1),
('ESTÁNDAR', 'Plan estándar mensual', 19990, 3),
('PREMIUM', 'Plan premium mensual', 29990, 10);

-- Servicios contratables
INSERT INTO servicio (nombre_servicio, descripcion, costo_mensual) VALUES
('Soporte Técnico', 'Asistencia técnica mensual', 4990),
('Respaldo en la nube', 'Backup mensual de información', 6990),
('Reportes avanzados', 'Módulo de reportes y estadísticas', 8990),
('Integración API', 'Acceso e integración a API externa', 12990);

-- Usuario ejemplo
INSERT INTO usuario (
    rut, nombres, apellido_paterno, apellido_materno, correo,
    telefono, direccion, ciudad, clave_hash, observacion
) VALUES (
    '16.650.344-2',
    'Pablo',
    'Vilches',
    'Valenzuela',
    'pablo@correo.cl',
    '+56912345678',
    'Calle Principal 123',
    'Santiago',
    'hash_seguro_aqui',
    'Usuario creado como ejemplo'
);

-- Suscripción ejemplo
INSERT INTO suscripcion (
    id_usuario, id_plan, fecha_inicio, fecha_fin, estado_suscripcion, renovacion_automatica
) VALUES (
    1, 2, CURRENT_DATE, CURRENT_DATE + INTERVAL '30 days', 'ACTIVA', TRUE
);

-- Servicios contratados ejemplo
INSERT INTO usuario_servicio (
    id_usuario, id_servicio, fecha_contratacion, estado, precio_pactado
) VALUES
(1, 1, CURRENT_DATE, 'ACTIVO', 4990),
(1, 2, CURRENT_DATE, 'ACTIVO', 6990);

-- Pago ejemplo
INSERT INTO pago (
    id_usuario, id_suscripcion, id_metodo_pago, periodo_anio, periodo_mes,
    monto_total, estado_pago, codigo_transaccion, observacion
) VALUES (
    1, 1, 1,
    EXTRACT(YEAR FROM CURRENT_DATE),
    EXTRACT(MONTH FROM CURRENT_DATE),
    31970,
    'PAGADO',
    'TRX-000001',
    'Pago del plan estándar y servicios adicionales'
);

-- Factura ejemplo
INSERT INTO factura (
    id_pago, numero_factura, fecha_emision, subtotal, impuesto, total, estado_factura
) VALUES (
    1, 'FAC-2026-0001', CURRENT_DATE, 31970, 0, 31970, 'PAGADA'
);

-- Detalle factura ejemplo
INSERT INTO detalle_factura (
    id_factura, tipo_item, descripcion_item, cantidad, precio_unitario, subtotal_item
) VALUES
(1, 'PLAN', 'Plan ESTÁNDAR', 1, 19990, 19990),
(1, 'SERVICIO', 'Soporte Técnico', 1, 4990, 4990),
(1, 'SERVICIO', 'Respaldo en la nube', 1, 6990, 6990);
