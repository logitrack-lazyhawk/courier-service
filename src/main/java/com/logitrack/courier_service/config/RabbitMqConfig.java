package com.logitrack.courier_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String EXCHANGE = "logitrack.assignment.events";
    public static final String COLA_SOLICITUDES = "courier.assignment.requests.q";
    public static final String COLA_RESULTADOS = "shipment.assignment.results.q";
    public static final String RK_SOLICITUD = "shipment.assignment.requested.v1";
    public static final String RK_CONFIRMADA = "courier.assignment.confirmed.v1";
    public static final String RK_RECHAZADA = "courier.assignment.rejected.v1";

    public static final String DEAD_LETTER_EXCHANGE = "logitrack.assignment.dlx";
    public static final String COLA_SOLICITUDES_DLQ = "courier.assignment.requests.dlq";
    public static final String RK_SOLICITUD_FALLIDA = "courier.assignment.requests.dead";

    @Bean
    public TopicExchange exchangeAsignaciones() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange exchangeDeadLetterAsignacion() {
        return new TopicExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue colaSolicitudes() {
        return QueueBuilder.durable(COLA_SOLICITUDES)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(RK_SOLICITUD_FALLIDA)
                .build();
    }

    @Bean
    public Queue colaSolicitudesDlq() {
        return QueueBuilder.durable(COLA_SOLICITUDES_DLQ).build();
    }

    @Bean
    public Queue colaResultados() {
        return QueueBuilder.durable(COLA_RESULTADOS).build();
    }

    @Bean
    public Binding bindingSolicitudes(
            @Qualifier("colaSolicitudes") Queue cola,
            @Qualifier("exchangeAsignaciones") TopicExchange exchange) {
        return BindingBuilder.bind(cola).to(exchange).with(RK_SOLICITUD);
    }

    @Bean
    public Binding bindingResultados(
            @Qualifier("colaResultados") Queue cola,
            @Qualifier("exchangeAsignaciones") TopicExchange exchange) {
        return BindingBuilder.bind(cola).to(exchange).with("courier.assignment.*.v1");
    }

    @Bean
    public Binding bindingSolicitudesDlq(
            @Qualifier("colaSolicitudesDlq") Queue cola,
            @Qualifier("exchangeDeadLetterAsignacion") TopicExchange exchange) {
        return BindingBuilder.bind(cola).to(exchange).with(RK_SOLICITUD_FALLIDA);
    }


    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter(
                "com.logitrack.courier_service.mensajeria.contrato"
        );
    }

}
