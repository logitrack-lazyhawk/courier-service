
package com.logitrack.courier_service.mensajeria.consumidor;

import com.logitrack.courier_service.config.RabbitMqConfig;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionResultadoV1;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionSolicitadaV1;
import com.logitrack.courier_service.repositorio.AsignacionProcesadaRepository;
import com.logitrack.courier_service.servicio.ProcesadorSolicitudAsignacion;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "spring.rabbitmq.host=127.0.0.1",
        "spring.rabbitmq.port=5673",
        "spring.rabbitmq.virtual-host=courier-consumo-it",
        "spring.rabbitmq.username=${RABBITMQ_USER:guest}",
        "spring.rabbitmq.password=${RABBITMQ_PASSWORD:guest}",
        "spring.rabbitmq.publisher-confirm-type=correlated",
        "spring.rabbitmq.publisher-returns=true",
        "spring.rabbitmq.template.mandatory=true",
        "spring.rabbitmq.listener.simple.auto-startup=true",
        "spring.rabbitmq.listener.simple.default-requeue-rejected=false",
        "spring.rabbitmq.listener.simple.retry.enabled=true",
        "spring.rabbitmq.listener.simple.retry.max-attempts=3",
        "spring.rabbitmq.listener.simple.retry.initial-interval=100ms",
        "spring.rabbitmq.listener.simple.retry.max-interval=200ms",
        "logitrack.rabbitmq.declare-on-startup=true"
})
class RabbitMqConsumoIT {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AsignacionProcesadaRepository repositorio;

    @MockitoBean
    private ProcesadorSolicitudAsignacion procesador;

    private AsignacionSolicitadaV1 solicitud() {
        return new AsignacionSolicitadaV1(
                UUID.randomUUID(),
                UUID.randomUUID(),
                1,
                Instant.now(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );
    }

    private AsignacionResultadoV1 resultado(AsignacionSolicitadaV1 s) {
        return new AsignacionResultadoV1(
                UUID.randomUUID(),
                s.solicitudId(),
                1,
                Instant.now(),
                s.envioId(),
                s.repartidorId(),
                true,
                null
        );
    }

    private void enviarComoShipment(AsignacionSolicitadaV1 evento) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE,
                RabbitMqConfig.RK_SOLICITUD,
                evento,
                mensaje -> {
                    mensaje.getMessageProperties().setHeader(
                            "__TypeId__",
                            "com.logitrack.shipmentservice.mensajeria.contrato.AsignacionSolicitadaV1"
                    );

                    mensaje.getMessageProperties().setDeliveryMode(
                            MessageDeliveryMode.PERSISTENT
                    );

                    return mensaje;
                }
        );
    }

    @Test
    void debeRecibirContratoDeShipment() {
        var evento = solicitud();

        when(procesador.procesar(any(AsignacionSolicitadaV1.class)))
                .thenAnswer(inv -> resultado(inv.getArgument(0)));

        enviarComoShipment(evento);

        Message respuesta = rabbitTemplate.receive(
                RabbitMqConfig.COLA_RESULTADOS, 10000
        );

        assertNotNull(respuesta);

        String json = new String(
                respuesta.getBody(),
                StandardCharsets.UTF_8
        );

        assertTrue(json.contains(evento.solicitudId().toString()));

        verify(procesador, times(1)).procesar(argThat(recibido ->
                recibido != null
                        && recibido.version() == 1
                        && evento.solicitudId().equals(recibido.solicitudId())
                        && evento.envioId().equals(recibido.envioId())
                        && evento.repartidorId().equals(recibido.repartidorId())
        ));
    }

    @Test
    void eventoDuplicadoNoDebeReprocesarAsignacion() {
        var evento = solicitud();
        long registrosAntes = repositorio.count();

        when(procesador.procesar(any(AsignacionSolicitadaV1.class)))
                .thenAnswer(inv -> resultado(inv.getArgument(0)));

        enviarComoShipment(evento);
        enviarComoShipment(evento);

        Message primeraRespuesta = rabbitTemplate.receive(
                RabbitMqConfig.COLA_RESULTADOS, 10000
        );

        Message segundaRespuesta = rabbitTemplate.receive(
                RabbitMqConfig.COLA_RESULTADOS, 10000
        );

        assertNotNull(primeraRespuesta);
        assertNotNull(segundaRespuesta);

        assertEquals(
                primeraRespuesta.getMessageProperties().getMessageId(),
                segundaRespuesta.getMessageProperties().getMessageId()
        );

        assertTrue(repositorio.existsById(evento.solicitudId()));
        assertEquals(registrosAntes + 1, repositorio.count());

        verify(procesador, times(1))
                .procesar(any(AsignacionSolicitadaV1.class));
    }

    @Test
    void eventoInvalidoDebeLlegarADlq() {
        var invalido = new AsignacionSolicitadaV1(
                UUID.randomUUID(),
                UUID.randomUUID(),
                2,
                Instant.now(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        enviarComoShipment(invalido);

        Message mensajeDlq = rabbitTemplate.receive(
                RabbitMqConfig.COLA_SOLICITUDES_DLQ, 10000
        );

        assertNotNull(mensajeDlq);

        String json = new String(
                mensajeDlq.getBody(),
                StandardCharsets.UTF_8
        );

        assertTrue(json.contains(invalido.solicitudId().toString()));

        assertNotNull(
                mensajeDlq.getMessageProperties()
                        .getHeaders().get("x-death")
        );

        assertFalse(repositorio.existsById(invalido.solicitudId()));
        verifyNoInteractions(procesador);
    }
}
