
package com.logitrack.courier_service.mensajeria.publicador;

import com.logitrack.courier_service.config.RabbitMqConfig;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionResultadoV1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publicador de resultados de asignación de envíos a través de RabbitMQ.
 * Envía mensajes a un exchange específico con la información del resultado.
 * @version 1.0.0
 * @author Equipo de LazyHawks
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class PublicadorResultadoAsignacionRabbitMq {

    private final RabbitTemplate rabbitTemplate;

    public void publicar(AsignacionResultadoV1 resultado) {

        String routingKey = resultado.aceptada()
                ? RabbitMqConfig.RK_CONFIRMADA
                : RabbitMqConfig.RK_RECHAZADA;

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE,
                routingKey,
                resultado,
                mensaje -> {
                    mensaje.getMessageProperties().setMessageId(
                            resultado.eventoId().toString()
                    );

                    mensaje.getMessageProperties().setDeliveryMode(
                            MessageDeliveryMode.PERSISTENT
                    );

                    return mensaje;
                }
        );

        log.info(
                "Resultado publicado. Solicitud={}, aceptada={}",
                resultado.solicitudId(),
                resultado.aceptada()
        );
    }
}
