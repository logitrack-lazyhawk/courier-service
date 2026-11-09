
package com.logitrack.courier_service.servicio;

import com.logitrack.courier_service.dominio.dto.RepartidorResponseDto;
import com.logitrack.courier_service.dominio.enumerador.EstadoRepartidor;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionResultadoV1;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionSolicitadaV1;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcesadorSolicitudAsignacion {

    private final RepartidorServicio repartidorServicio;

    public AsignacionResultadoV1 procesar(
            AsignacionSolicitadaV1 evento) {

        validarEvento(evento);

        try {
            RepartidorResponseDto repartidor =
                    repartidorServicio.obtenerDisponibilidad(
                            evento.repartidorId()
                    );

            if (repartidor == null ||
                    repartidor.estado() != EstadoRepartidor.ACTIVO) {

                return crearResultado(
                        evento,
                        false,
                        "El repartidor no se encuentra ACTIVO."
                );
            }

            return crearResultado(evento, true, null);

        } catch (IllegalArgumentException ex) {

            return crearResultado(
                    evento,
                    false,
                    ex.getMessage()
            );
        }
    }

    private void validarEvento(AsignacionSolicitadaV1 evento) {

        if (evento == null ||
                evento.eventoId() == null ||
                evento.solicitudId() == null ||
                evento.version() != 1 ||
                evento.ocurridoEn() == null ||
                evento.envioId() == null ||
                evento.repartidorId() == null) {

            throw new IllegalArgumentException(
                    "Evento de asignación inválido o incompatible."
            );
        }
    }

    private AsignacionResultadoV1 crearResultado(
            AsignacionSolicitadaV1 evento,
            boolean aceptada,
            String motivo) {

        return new AsignacionResultadoV1(
                UUID.randomUUID(),
                evento.solicitudId(),
                1,
                Instant.now(),
                evento.envioId(),
                evento.repartidorId(),
                aceptada,
                motivo
        );
    }
}
