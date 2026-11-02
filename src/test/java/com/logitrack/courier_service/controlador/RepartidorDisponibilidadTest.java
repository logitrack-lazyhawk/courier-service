package com.logitrack.courier_service.controlador;

import com.logitrack.courier_service.controlador.exception.GlobalExceptionHandler;
import com.logitrack.courier_service.dominio.exception.RepartidorNoEncontradoException;
import com.logitrack.courier_service.servicio.RepartidorServicio;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RepartidorDisponibilidadTest {
    @Test
    void distinguishesMissingCourierFromInactiveCourier() throws Exception {
        var servicio = mock(RepartidorServicio.class);
        var mvc = MockMvcBuilders.standaloneSetup(new RepartidorController(servicio))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        var id = UUID.randomUUID();
        when(servicio.obtenerDisponibilidad(id)).thenThrow(new RepartidorNoEncontradoException("No existe"));
        mvc.perform(get("/api/v1/repartidores/{id}/disponibilidad", id))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
        doThrow(new IllegalArgumentException("INACTIVO")).when(servicio).obtenerDisponibilidad(id);
        mvc.perform(get("/api/v1/repartidores/{id}/disponibilidad", id))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }
}
