-- ==============================================================================
-- LOGITRACK - COURIER SERVICE
-- Script de inicialización de datos para repartidores (UUIDs almacenados como BINARY(16))
-- ==============================================================================

INSERT IGNORE INTO repartidores (id, nombre, dni, email, telefono, tipo_vehiculo, estado, fecha_creacion, fecha_actualizacion, fecha_eliminacion)
VALUES 
    (UUID_TO_BIN('11111111-1111-1111-1111-111111111111'), 'Carlos Gómez', '30111222', 'carlos.gomez@logitrack.com', '+541144332211', 'Moto', 'ACTIVO', NOW(), NOW(), NULL),
    (UUID_TO_BIN('22222222-2222-2222-2222-222222222222'), 'Mariana López', '31222333', 'mariana.lopez@logitrack.com', '+541155443322', 'Camioneta', 'ACTIVO', NOW(), NOW(), NULL),
    (UUID_TO_BIN('33333333-3333-3333-3333-333333333333'), 'Juan Pérez', '32333444', 'juan.perez@logitrack.com', '+541166554433', 'Bicicleta', 'ACTIVO', NOW(), NOW(), NULL),
    (UUID_TO_BIN('44444444-4444-4444-4444-444444444444'), 'Lucía Fernández', '33444555', 'lucia.fernandez@logitrack.com', '+541177665544', 'Auto', 'ACTIVO', NOW(), NOW(), NULL),
    (UUID_TO_BIN('55555555-5555-5555-5555-555555555555'), 'Roberto Díaz', '34555666', 'roberto.diaz@logitrack.com', '+541188776655', 'Utilitario', 'INACTIVO', NOW(), NOW(), NULL);
