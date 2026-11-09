
package com.logitrack.courier_service.mensajeria.consumidor;

import com.logitrack.courier_service.mensajeria.contrato.AsignacionResultadoV1;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionSolicitadaV1;
import com.logitrack.courier_service.servicio.ProcesadorSolicitudAsignacion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.logitrack.courier_service.mensajeria.publicador.PublicadorResultadoAsignacionRabbitMq;

@ExtendWith(MockitoExtension.class)
class ConsumidorAsignacionRabbitMqTest {

    @Mock
    private ProcesadorSolicitudAsignacion procesador;

    @Mock
    private PublicadorResultadoAsignacionRabbitMq publicador;

    @InjectMocks
    private ConsumidorAsignacionRabbitMq consumidor;

    private AsignacionSolicitadaV1 crearEvento() {
        return new AsignacionSolicitadaV1(
                UUID.randomUUID(),
                UUID.randomUUID(),
                1,
                Instant.now(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );
    }

    private AsignacionResultadoV1 crearResultado(
            AsignacionSolicitadaV1 evento,
            boolean aceptada,
            String motivo) {

        return new AsignacionResultadoV1(
                UUID.randomUUID(),
                evento.solicitudId(),
                1,
                Instant.now(),
                evento.envioId(),
                evento.repartidorId(),
                aceptada,
                motivo
        );
    }

    @Test
    void debeProcesarSolicitudAceptada() {
        var evento = crearEvento();
        var resultado = crearResultado(evento, true, null);

        when(procesador.procesar(evento)).thenReturn(resultado);

        assertDoesNotThrow(() -> consumidor.consumir(evento));

        verify(procesador).procesar(evento);
    }

    @Test
    void debeProcesarSolicitudRechazada() {
        var evento = crearEvento();
        var resultado = crearResultado(
                evento, false, "Repartidor INACTIVO"
        );

        when(procesador.procesar(evento)).thenReturn(resultado);

        assertDoesNotThrow(() -> consumidor.consumir(evento));

        verify(procesador).procesar(evento);
        verify(publicador).publicar(resultado);
    }

    @Test
    void debeRechazarEventoInvalidoSinReencolar() {
        var evento = crearEvento();

        when(procesador.procesar(evento))
                .thenThrow(new IllegalArgumentException(
                        "Evento inválido"
                ));

        assertThrows(
                AmqpRejectAndDontRequeueException.class,
                () -> consumidor.consumir(evento)
        );
    }
}
