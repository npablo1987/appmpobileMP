-- Tabla para almacenar la relación entre usuarios y customers de Mercado Pago
CREATE TABLE IF NOT EXISTS mp_customer (
    id SERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL UNIQUE REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    mp_customer_id VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_mp_customer_id_usuario ON mp_customer(id_usuario);
CREATE INDEX idx_mp_customer_mp_customer_id ON mp_customer(mp_customer_id);

-- Tabla para almacenar tarjetas guardadas (solo referencias seguras, sin datos sensibles)
CREATE TABLE IF NOT EXISTS tarjeta_guardada (
    id SERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    mp_customer_id VARCHAR(100) NOT NULL,
    mp_card_id VARCHAR(100) NOT NULL UNIQUE,
    payment_method_id VARCHAR(50) NOT NULL,
    brand VARCHAR(50),
    last_four_digits VARCHAR(4) NOT NULL,
    expiration_month INTEGER NOT NULL,
    expiration_year INTEGER NOT NULL,
    holder_name VARCHAR(200) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tarjeta_guardada_id_usuario ON tarjeta_guardada(id_usuario);
CREATE INDEX idx_tarjeta_guardada_mp_customer_id ON tarjeta_guardada(mp_customer_id);
CREATE INDEX idx_tarjeta_guardada_mp_card_id ON tarjeta_guardada(mp_card_id);
CREATE INDEX idx_tarjeta_guardada_is_default ON tarjeta_guardada(id_usuario, is_default);

-- Trigger para actualizar updated_at automáticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_mp_customer_updated_at
    BEFORE UPDATE ON mp_customer
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tarjeta_guardada_updated_at
    BEFORE UPDATE ON tarjeta_guardada
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Comentarios para documentación
COMMENT ON TABLE mp_customer IS 'Relación entre usuarios locales y customers de Mercado Pago';
COMMENT ON TABLE tarjeta_guardada IS 'Tarjetas guardadas de usuarios (solo referencias seguras, sin PAN ni CVV)';
COMMENT ON COLUMN tarjeta_guardada.mp_card_id IS 'ID de la tarjeta en Mercado Pago';
COMMENT ON COLUMN tarjeta_guardada.last_four_digits IS 'Últimos 4 dígitos de la tarjeta para identificación visual';
COMMENT ON COLUMN tarjeta_guardada.is_default IS 'Indica si es la tarjeta predeterminada del usuario';
