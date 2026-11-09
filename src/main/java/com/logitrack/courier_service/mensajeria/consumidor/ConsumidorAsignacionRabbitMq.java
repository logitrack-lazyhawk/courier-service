
package com.logitrack.courier_service.mensajeria.consumidor;

import com.logitrack.courier_service.config.RabbitMqConfig;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionResultadoV1;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionSolicitadaV1;
import com.logitrack.courier_service.mensajeria.publicador.PublicadorResultadoAsignacionRabbitMq;
import com.logitrack.courier_service.servicio.ProcesadorSolicitudAsignacion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor de solicitudes de asignación de envíos a través de RabbitMQ.
 * Escucha mensajes en una cola específica y procesa las solicitudes.
 * @version 1.0.0
 * @author Equipo de LazyHawks
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumidorAsignacionRabbitMq {

    private final ProcesadorSolicitudAsignacion procesador;
    private final PublicadorResultadoAsignacionRabbitMq publicador;

    @RabbitListener(queues = RabbitMqConfig.COLA_SOLICITUDES)
    public void consumir(AsignacionSolicitadaV1 evento) {

        AsignacionResultadoV1 resultado;

        try {
            resultado = procesador.procesar(evento);

        } catch (IllegalArgumentException ex) {

            log.error(
                    "Evento de solicitud inválido: {}",
                    ex.getMessage()
            );

            throw new AmqpRejectAndDontRequeueException(
                    "No se puede procesar la solicitud.",
                    ex
            );
        }

        publicador.publicar(resultado);

        log.info(
                "Solicitud {} procesada. Resultado aceptado={}",
                resultado.solicitudId(),
                resultado.aceptada()
        );
    }
}
