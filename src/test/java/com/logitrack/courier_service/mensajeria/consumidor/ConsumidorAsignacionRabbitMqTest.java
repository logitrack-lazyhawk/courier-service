package com.logitrack.courier_service.mensajeria.consumidor;

import com.logitrack.courier_service.mensajeria.contrato.AsignacionResultadoV1;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionSolicitadaV1;
import com.logitrack.courier_service.mensajeria.publicador.PublicadorResultadoAsignacionRabbitMq;
import com.logitrack.courier_service.servicio.ProcesadorIdempotenteAsignacion;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsumidorAsignacionRabbitMqTest {
    @Mock private ProcesadorIdempotenteAsignacion procesador;
    @Mock private PublicadorResultadoAsignacionRabbitMq publicador;
    @InjectMocks private ConsumidorAsignacionRabbitMq consumidor;

    private AsignacionSolicitadaV1 solicitud() {
        return new AsignacionSolicitadaV1(UUID.randomUUID(), UUID.randomUUID(), 1,
                Instant.now(), UUID.randomUUID(), UUID.randomUUID());
    }

    private AsignacionResultadoV1 resultado(AsignacionSolicitadaV1 s, boolean aceptada) {
        return new AsignacionResultadoV1(UUID.randomUUID(), s.solicitudId(), 1,
                Instant.now(), s.envioId(), s.repartidorId(), aceptada,
                aceptada ? null : "Repartidor INACTIVO");
    }

    @Test
    void debePublicarSolicitudAceptada() {
        var s = solicitud(); var r = resultado(s, true);
        when(procesador.procesar(s)).thenReturn(r);
        consumidor.consumir(s);
        verify(publicador).publicar(r);
    }

    @Test
    void debePublicarSolicitudRechazada() {
        var s = solicitud(); var r = resultado(s, false);
        when(procesador.procesar(s)).thenReturn(r);
        consumidor.consumir(s);
        verify(publicador).publicar(r);
    }

    @Test
    void debeRechazarEventoInvalidoSinReencolar() {
        var s = solicitud();
        when(procesador.procesar(s)).thenThrow(new IllegalArgumentException("Evento inválido"));
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> consumidor.consumir(s));
        verifyNoInteractions(publicador);
    }

    @Test
    void falloTemporalDebePropagarseParaReintento() {
        var s = solicitud(); var r = resultado(s, true);
        when(procesador.procesar(s)).thenReturn(r);
        doThrow(new IllegalStateException("Rabbit sin conexión")).when(publicador).publicar(r);
        assertThrows(IllegalStateException.class, () -> consumidor.consumir(s));
    }
}
