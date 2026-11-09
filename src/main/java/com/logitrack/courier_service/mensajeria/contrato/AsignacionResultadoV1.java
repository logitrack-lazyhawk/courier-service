
package com.logitrack.courier_service.mensajeria.contrato;

import java.time.Instant;
import java.util.UUID;

public record AsignacionResultadoV1(
        UUID eventoId,
        UUID solicitudId,
        int version,
        Instant ocurridoEn,
        UUID envioId,
        UUID repartidorId,
        boolean aceptada,
        String motivo
) {}
