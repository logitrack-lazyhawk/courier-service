package com.logitrack.courier_service.servicio;

import com.logitrack.courier_service.dominio.entidad.Repartidor;
import com.logitrack.courier_service.dominio.enumerador.EstadoRepartidor;
import com.logitrack.courier_service.repositorio.RepartidorRepository;
import com.logitrack.courier_service.servicio.impl.RepartidorServicioImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepartidorServicioTest {

    @Mock
    private RepartidorRepository repartidorRepository;

    @InjectMocks
    private RepartidorServicioImpl repartidorServicio;

    @Test
    @DisplayName("Debe lanzar excepción si el repartidor está INACTIVO")
    void obtenerDisponibilidad_CuandoInactivo_LanzaExcepcion() {
        // 1. Preparar datos
        UUID id = UUID.randomUUID();
        Repartidor repartidor = new Repartidor();
        repartidor.setId(id);
        repartidor.setEstado(EstadoRepartidor.INACTIVO);

        // 2. Simular el repositorio
        when(repartidorRepository.findById(id)).thenReturn(Optional.of(repartidor));

        // 3. Ejecutar y verificar
        assertThrows(IllegalArgumentException.class, () -> repartidorServicio.obtenerDisponibilidad(id));
    }
}
