package com.logitrack.courier_service.servicio;

import com.logitrack.courier_service.mensajeria.contrato.AsignacionResultadoV1;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionSolicitadaV1;
import com.logitrack.courier_service.repositorio.AsignacionProcesadaRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest
@Import(ProcesadorIdempotenteAsignacion.class)
class ProcesadorIdempotenteAsignacionTest {
    @Autowired private ProcesadorIdempotenteAsignacion idempotencia;
    @Autowired private AsignacionProcesadaRepository repositorio;
    @MockitoBean private ProcesadorSolicitudAsignacion procesador;

    private AsignacionSolicitadaV1 solicitud() {
        return new AsignacionSolicitadaV1(UUID.randomUUID(), UUID.randomUUID(), 1,
                Instant.now(), UUID.randomUUID(), UUID.randomUUID());
    }

    private AsignacionResultadoV1 resultado(AsignacionSolicitadaV1 s) {
        return new AsignacionResultadoV1(UUID.randomUUID(), s.solicitudId(), 1,
                Instant.now(), s.envioId(), s.repartidorId(), true, null);
    }

    @Test
    void dosEntregasDebenUsarUnMismoResultadoPersistido() {
        var s = solicitud(); var r = resultado(s);
        when(procesador.procesar(s)).thenReturn(r);
        assertEquals(r, idempotencia.procesar(s));
        assertEquals(r, idempotencia.procesar(s));
        assertTrue(repositorio.existsById(s.solicitudId()));
        verify(procesador, times(1)).procesar(s);
    }

    @Test
    void mismoSolicitudIdConNuevoEventoIdNoDebeReprocesar() {
        var s = solicitud(); var r = resultado(s);
        when(procesador.procesar(s)).thenReturn(r);
        idempotencia.procesar(s);
        var repetido = new AsignacionSolicitadaV1(UUID.randomUUID(), s.solicitudId(), 1,
                Instant.now(), s.envioId(), s.repartidorId());
        assertEquals(r, idempotencia.procesar(repetido));
        verify(procesador, times(1)).procesar(any());
    }

    @Test
    void mismoSolicitudIdConDistintoRepartidorDebeFallar() {
        var s = solicitud(); var r = resultado(s);
        when(procesador.procesar(s)).thenReturn(r);
        idempotencia.procesar(s);
        var alterado = new AsignacionSolicitadaV1(UUID.randomUUID(), s.solicitudId(), 1,
                Instant.now(), s.envioId(), UUID.randomUUID());
        assertThrows(IllegalArgumentException.class, () -> idempotencia.procesar(alterado));
        verify(procesador, times(1)).procesar(any());
    }

    @Test
    void eventoInvalidoNoDebePersistirse() {
        var s = new AsignacionSolicitadaV1(UUID.randomUUID(), UUID.randomUUID(), 2,
                Instant.now(), UUID.randomUUID(), UUID.randomUUID());
        assertThrows(IllegalArgumentException.class, () -> idempotencia.procesar(s));
        assertFalse(repositorio.existsById(s.solicitudId()));
        verifyNoInteractions(procesador);
    }
}
