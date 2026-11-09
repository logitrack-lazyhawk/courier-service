
package com.logitrack.courier_service.servicio;

import com.logitrack.courier_service.dominio.dto.RepartidorResponseDto;
import com.logitrack.courier_service.dominio.enumerador.EstadoRepartidor;
import com.logitrack.courier_service.dominio.exception.RepartidorNoEncontradoException;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionSolicitadaV1;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcesadorSolicitudAsignacionTest {

    @Mock
    private RepartidorServicio repartidorServicio;

    @InjectMocks
    private ProcesadorSolicitudAsignacion procesador;

    private AsignacionSolicitadaV1 crearEvento(UUID repartidorId) {
        return new AsignacionSolicitadaV1(
                UUID.randomUUID(),
                UUID.randomUUID(),
                1,
                Instant.now(),
                UUID.randomUUID(),
                repartidorId
        );
    }

    @Test
    void debeAceptarRepartidorActivo() {
        UUID id = UUID.randomUUID();
        var evento = crearEvento(id);

        var repartidor = new RepartidorResponseDto(
                id, "Prueba", "12345678",
                "prueba@example.com", "3884000000",
                "Moto", EstadoRepartidor.ACTIVO
        );

        when(repartidorServicio.obtenerDisponibilidad(id))
                .thenReturn(repartidor);

        var resultado = procesador.procesar(evento);

        assertTrue(resultado.aceptada());
        assertNull(resultado.motivo());
        assertEquals(evento.solicitudId(), resultado.solicitudId());
        assertEquals(evento.envioId(), resultado.envioId());
        assertEquals(id, resultado.repartidorId());
        assertEquals(1, resultado.version());
    }

    @Test
    void debeRechazarRepartidorInactivo() {
        UUID id = UUID.randomUUID();
        var evento = crearEvento(id);

        when(repartidorServicio.obtenerDisponibilidad(id))
                .thenThrow(new IllegalArgumentException(
                        "El repartidor se encuentra INACTIVO."
                ));

        var resultado = procesador.procesar(evento);

        assertFalse(resultado.aceptada());
        assertTrue(resultado.motivo().contains("INACTIVO"));
    }

    @Test
    void debeRechazarRepartidorInexistente() {
        UUID id = UUID.randomUUID();
        var evento = crearEvento(id);

        when(repartidorServicio.obtenerDisponibilidad(id))
                .thenThrow(new RepartidorNoEncontradoException(
                        "Repartidor no encontrado"
                ));

        var resultado = procesador.procesar(evento);

        assertFalse(resultado.aceptada());
        assertEquals("Repartidor no encontrado", resultado.motivo());
    }

    @Test
    void debeRechazarVersionIncompatible() {
        var evento = new AsignacionSolicitadaV1(
                UUID.randomUUID(),
                UUID.randomUUID(),
                2,
                Instant.now(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> procesador.procesar(evento)
        );

        verifyNoInteractions(repartidorServicio);
    }

    @Test
    void debeRechazarEventoSinRepartidor() {
        var evento = crearEvento(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> procesador.procesar(evento)
        );

        verifyNoInteractions(repartidorServicio);
    }
}
