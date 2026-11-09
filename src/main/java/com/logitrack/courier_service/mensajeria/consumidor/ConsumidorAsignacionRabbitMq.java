
package com.logitrack.courier_service.mensajeria.consumidor;

import com.logitrack.courier_service.config.RabbitMqConfig;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionResultadoV1;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionSolicitadaV1;
import com.logitrack.courier_service.servicio.ProcesadorSolicitudAsignacion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumidorAsignacionRabbitMq {

    private final ProcesadorSolicitudAsignacion procesador;

    @RabbitListener(queues = RabbitMqConfig.COLA_SOLICITUDES)
    public void consumir(AsignacionSolicitadaV1 evento) {

        try {
            AsignacionResultadoV1 resultado =
                    procesador.procesar(evento);

            if (resultado.aceptada()) {

                log.info(
                        "Solicitud {} validada. Repartidor {} disponible.",
                        resultado.solicitudId(),
                        resultado.repartidorId()
                );

            } else {

                log.warn(
                        "Solicitud {} rechazada. Motivo: {}",
                        resultado.solicitudId(),
                        resultado.motivo()
                );
            }

        } catch (IllegalArgumentException ex) {

            log.error(
                    "Evento de asignación inválido: {}",
                    ex.getMessage()
            );

            throw new AmqpRejectAndDontRequeueException(
                    "No se puede procesar el evento de asignación.",
                    ex
            );
        }
    }
}
