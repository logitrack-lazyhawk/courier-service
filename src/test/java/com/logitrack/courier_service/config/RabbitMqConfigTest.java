package com.logitrack.courier_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import static org.junit.jupiter.api.Assertions.*;


import com.logitrack.courier_service.mensajeria.contrato.AsignacionSolicitadaV1;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionResultadoV1;
import org.springframework.amqp.core.MessageProperties;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;


class RabbitMqConfigTest {
    private final RabbitMqConfig config = new RabbitMqConfig();

    @Test
    void exchangeDebeSerTopicYDurable() {
        var exchange = config.exchangeAsignaciones();
        assertEquals("topic", exchange.getType());
        assertTrue(exchange.isDurable());
        assertFalse(exchange.isAutoDelete());
    }

    @Test
    void colasDebenSerDurables() {
        assertTrue(config.colaSolicitudes().isDurable());
        assertTrue(config.colaResultados().isDurable());
        assertTrue(config.colaSolicitudesDlq().isDurable());
    }

    @Test
    void solicitudDebeTenerRoutingKeyVersionada() {
        Binding binding = config.bindingSolicitudes(config.colaSolicitudes(), config.exchangeAsignaciones());
        assertEquals(RabbitMqConfig.RK_SOLICITUD, binding.getRoutingKey());
    }

    @Test
    void resultadosDebenCompartirCola() {
        Binding binding = config.bindingResultados(config.colaResultados(), config.exchangeAsignaciones());
        assertEquals("courier.assignment.*.v1", binding.getRoutingKey());
    }

    @Test
    void debeConfigurarDeadLetterQueue() {
        var cola = config.colaSolicitudes();
        assertEquals(RabbitMqConfig.DEAD_LETTER_EXCHANGE,
                cola.getArguments().get("x-dead-letter-exchange"));
        assertEquals(RabbitMqConfig.RK_SOLICITUD_FALLIDA,
                cola.getArguments().get("x-dead-letter-routing-key"));
        assertEquals("topic", config.exchangeDeadLetterAsignacion().getType());
        assertTrue(config.exchangeDeadLetterAsignacion().isDurable());
        assertEquals(RabbitMqConfig.RK_SOLICITUD_FALLIDA,
                config.bindingSolicitudesDlq(config.colaSolicitudesDlq(),
                        config.exchangeDeadLetterAsignacion()).getRoutingKey());
    }

    @Test
    void convertidorDebeUtilizarJackson() {
        assertInstanceOf(JacksonJsonMessageConverter.class, config.rabbitMessageConverter());
    }


    @Test
    void contratosVersionUnoDebenSerSerializables() {
        var solicitud = new AsignacionSolicitadaV1(
                UUID.randomUUID(),
                UUID.randomUUID(),
                1,
                Instant.now(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        var resultado = new AsignacionResultadoV1(
                UUID.randomUUID(),
                solicitud.solicitudId(),
                1,
                Instant.now(),
                solicitud.envioId(),
                solicitud.repartidorId(),
                true,
                null
        );

        var converter = config.rabbitMessageConverter();

        var mensajeSolicitud = converter.toMessage(
                solicitud, new MessageProperties()
        );

        var mensajeResultado = converter.toMessage(
                resultado, new MessageProperties()
        );

        String jsonSolicitud = new String(
                mensajeSolicitud.getBody(), StandardCharsets.UTF_8
        );

        String jsonResultado = new String(
                mensajeResultado.getBody(), StandardCharsets.UTF_8
        );

        for (String campo : new String[] {
                "eventoId", "solicitudId", "version",
                "ocurridoEn", "envioId", "repartidorId"
        }) {
            assertTrue(jsonSolicitud.contains("\"" + campo + "\""));
            assertTrue(jsonResultado.contains("\"" + campo + "\""));
        }

        assertTrue(jsonResultado.contains("\"aceptada\""));
        assertTrue(jsonResultado.contains("\"motivo\""));

        assertEquals(solicitud, converter.fromMessage(mensajeSolicitud));
        assertEquals(resultado, converter.fromMessage(mensajeResultado));
    }

}
