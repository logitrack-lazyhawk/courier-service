-- Ejecutar solo si su MySQL NO crea el esquema automáticamente con Hibernate.
-- Verifique primero el nombre de base de datos de courier-service.
CREATE TABLE IF NOT EXISTS asignaciones_procesadas (
    solicitud_id BINARY(16) NOT NULL,
    evento_solicitud_id BINARY(16) NOT NULL,
    evento_resultado_id BINARY(16) NOT NULL,
    envio_id BINARY(16) NOT NULL,
    repartidor_id BINARY(16) NOT NULL,
    aceptada BIT(1) NOT NULL,
    motivo VARCHAR(1024) NULL,
    ocurrido_en DATETIME(6) NOT NULL,
    PRIMARY KEY (solicitud_id)
) ENGINE=InnoDB;
