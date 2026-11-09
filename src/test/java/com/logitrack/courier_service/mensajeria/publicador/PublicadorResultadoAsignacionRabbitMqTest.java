package com.logitrack.courier_service.mensajeria.publicador;

import com.logitrack.courier_service.config.RabbitMqConfig;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionResultadoV1;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicadorResultadoAsignacionRabbitMqTest {
    @Mock private RabbitTemplate rabbitTemplate;
    @InjectMocks private PublicadorResultadoAsignacionRabbitMq publicador;

    private AsignacionResultadoV1 resultado(boolean aceptada) {
        return new AsignacionResultadoV1(UUID.randomUUID(), UUID.randomUUID(), 1,
                Instant.now(), UUID.randomUUID(), UUID.randomUUID(),
                aceptada, aceptada ? null : "No disponible");
    }

    private void simularConfirmacion(boolean ack) {
        doAnswer(invocacion -> {
            CorrelationData correlation = invocacion.getArgument(4);
            correlation.getFuture().complete(new CorrelationData.Confirm(ack, ack ? null : "Broker NACK"));
            return null;
        }).when(rabbitTemplate).convertAndSend(
                anyString(), anyString(), any(AsignacionResultadoV1.class),
                any(MessagePostProcessor.class), any(CorrelationData.class));
    }

    @Test
    void ackPositivoPermiteCompletarPublicacion() {
        var r = resultado(true);
        simularConfirmacion(true);
        assertDoesNotThrow(() -> publicador.publicar(r));
        verify(rabbitTemplate).convertAndSend(eq(RabbitMqConfig.EXCHANGE),
                eq(RabbitMqConfig.RK_CONFIRMADA), eq(r),
                any(MessagePostProcessor.class), any(CorrelationData.class));
    }

    @Test
    void nackProvocaErrorParaReintentar() {
        var r = resultado(false);
        simularConfirmacion(false);
        assertThrows(IllegalStateException.class, () -> publicador.publicar(r));
        verify(rabbitTemplate).convertAndSend(eq(RabbitMqConfig.EXCHANGE),
                eq(RabbitMqConfig.RK_RECHAZADA), eq(r),
                any(MessagePostProcessor.class), any(CorrelationData.class));
    }
}
