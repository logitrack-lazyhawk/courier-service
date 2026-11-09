package com.logitrack.courier_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import static org.junit.jupiter.api.Assertions.*;

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
}
