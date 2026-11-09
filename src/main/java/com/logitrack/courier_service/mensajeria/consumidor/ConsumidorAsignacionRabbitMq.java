package com.logitrack.courier_service.mensajeria.consumidor;

import com.logitrack.courier_service.config.RabbitMqConfig;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionResultadoV1;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionSolicitadaV1;
import com.logitrack.courier_service.mensajeria.publicador.PublicadorResultadoAsignacionRabbitMq;
import com.logitrack.courier_service.servicio.ProcesadorIdempotenteAsignacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumidorAsignacionRabbitMq {

    private final ProcesadorIdempotenteAsignacion procesador;
    private final PublicadorResultadoAsignacionRabbitMq publicador;

    @RabbitListener(queues = RabbitMqConfig.COLA_SOLICITUDES)
    public void consumir(AsignacionSolicitadaV1 evento) {
        final AsignacionResultadoV1 resultado;
        try {
            resultado = procesador.procesar(evento);
        } catch (IllegalArgumentException ex) {
            log.error("Solicitud inválida: {}", ex.getMessage());
            // Sin reencolado infinito. RabbitMQ puede enviarla a la DLQ.
            throw new AmqpRejectAndDontRequeueException("Solicitud inválida", ex);
        }

        // Un error de conexión, NACK o timeout DEBE propagarse para activar
        // los reintentos. La solicitud persiste para ser retomada luego.
        publicador.publicar(resultado);
        log.info("Solicitud {} procesada, aceptada={}",
                resultado.solicitudId(), resultado.aceptada());
    }
}
