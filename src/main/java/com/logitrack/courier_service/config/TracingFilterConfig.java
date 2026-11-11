package com.logitrack.courier_service.config;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;

/**
 * Excluye las llamadas periódicas de Prometheus y Actuator de Zipkin.
 */
@Configuration
public class TracingFilterConfig {

    @Bean
    public ObservationPredicate excludeActuatorFromTracing() {
        return (name, context) -> {
            if ("http.server.requests".equals(name) && context instanceof ServerRequestObservationContext serverContext) {
                String uri = serverContext.getCarrier().getRequestURI();
                if (uri != null && uri.startsWith("/actuator")) {
                    return false;
                }
            }
            return true;
        };
    }
}

