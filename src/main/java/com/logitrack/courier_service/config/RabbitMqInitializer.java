
package com.logitrack.courier_service.config;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
public class RabbitMqInitializer {

    @Bean
    @ConditionalOnProperty(
            name = "logitrack.rabbitmq.declare-on-startup",
            havingValue = "true"
    )
    public ApplicationRunner inicializarRabbitMq(
            AmqpAdmin amqpAdmin) {

        return args -> amqpAdmin.initialize();
    }
}
