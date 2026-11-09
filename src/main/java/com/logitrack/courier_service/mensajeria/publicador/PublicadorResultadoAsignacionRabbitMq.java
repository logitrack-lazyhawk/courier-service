package com.logitrack.courier_service.mensajeria.publicador;

import com.logitrack.courier_service.config.RabbitMqConfig;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionResultadoV1;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublicadorResultadoAsignacionRabbitMq {

    private final RabbitTemplate rabbitTemplate;

    public void publicar(AsignacionResultadoV1 resultado) {
        String routingKey = resultado.aceptada()
                ? RabbitMqConfig.RK_CONFIRMADA : RabbitMqConfig.RK_RECHAZADA;
        // La correlación de cada intento es única; el ID lógico del evento se conserva.
        CorrelationData correlacion = new CorrelationData(UUID.randomUUID().toString());

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE,
                routingKey,
                resultado,
                mensaje -> {
                    mensaje.getMessageProperties().setMessageId(resultado.eventoId().toString());
                    mensaje.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return mensaje;
                },
                correlacion);

        try {
            CorrelationData.Confirm confirmacion = correlacion.getFuture().get(5, TimeUnit.SECONDS);
            if (!confirmacion.ack()) {
                throw new IllegalStateException("RabbitMQ rechazó el resultado: " + confirmacion.reason());
            }
            if (correlacion.getReturned() != null) {
                throw new IllegalStateException("Resultado sin cola de destino (mensaje devuelto).");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Publicación interrumpida", ex);
        } catch (ExecutionException | TimeoutException ex) {
            throw new IllegalStateException("No se confirmó publicación en RabbitMQ", ex);
        }

        log.info("Resultado confirmado por RabbitMQ. Solicitud={}, aceptada={}",
                resultado.solicitudId(), resultado.aceptada());
    }
}
