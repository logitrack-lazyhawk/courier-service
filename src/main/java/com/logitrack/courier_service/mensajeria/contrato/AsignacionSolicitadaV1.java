
package com.logitrack.courier_service.mensajeria.contrato;

import java.time.Instant;
import java.util.UUID;

public record AsignacionSolicitadaV1(
        UUID eventoId,
        UUID solicitudId,
        int version,
        Instant ocurridoEn,
        UUID envioId,
        UUID repartidorId
) {}
