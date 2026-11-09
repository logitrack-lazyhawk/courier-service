
package com.logitrack.courier_service.mensajeria.consumidor;

import com.logitrack.courier_service.config.RabbitMqConfig;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionResultadoV1;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionSolicitadaV1;
import com.logitrack.courier_service.servicio.ProcesadorSolicitudAsignacion;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
/**
 * Prueba de integración para el consumidor de RabbitMQ.
 * Verifica que los eventos enviados a RabbitMQ sean recibidos y procesados correctamente.
 * @author Equipo de LazyHawks
 * @version 1.0.0
 */
@SpringBootTest(properties = {
        "spring.rabbitmq.host=127.0.0.1",
        "spring.rabbitmq.port=5673",
        "spring.rabbitmq.username=${RABBITMQ_USER:guest}",
        "spring.rabbitmq.password=${RABBITMQ_PASSWORD:guest}",
        "spring.rabbitmq.listener.simple.auto-startup=true"
})
class RabbitMqConsumoIT {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private ProcesadorSolicitudAsignacion procesador;

    @Test
    void debeRecibirEventoDesdeRabbitMq() {

        var evento = new AsignacionSolicitadaV1(
                UUID.randomUUID(),
                UUID.randomUUID(),
                1,
                Instant.now(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        when(procesador.procesar(any(AsignacionSolicitadaV1.class)))
                .thenAnswer(invocacion -> {

                    AsignacionSolicitadaV1 recibido =
                            invocacion.getArgument(0);

                    return new AsignacionResultadoV1(
                            UUID.randomUUID(),
                            recibido.solicitudId(),
                            1,
                            Instant.now(),
                            recibido.envioId(),
                            recibido.repartidorId(),
                            true,
                            null
                    );
                });

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE,
                RabbitMqConfig.RK_SOLICITUD,
                evento,
                mensaje -> {
                    mensaje.getMessageProperties().setHeader(
                            "__TypeId__",
                            "com.logitrack.shipmentservice.mensajeria.contrato.AsignacionSolicitadaV1"
                    );
                    return mensaje;
                }
        );

        verify(procesador, timeout(10000))
                .procesar(argThat(recibido ->
                        recibido != null
                                && evento.solicitudId().equals(recibido.solicitudId())
                                && evento.envioId().equals(recibido.envioId())
                                && evento.repartidorId().equals(recibido.repartidorId())
                ));
    }
}
