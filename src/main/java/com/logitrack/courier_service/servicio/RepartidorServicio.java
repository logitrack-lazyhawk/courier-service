package com.logitrack.courier_service.servicio;

import com.logitrack.courier_service.dominio.dto.RepartidorRequestDto;
import com.logitrack.courier_service.dominio.dto.RepartidorResponseDto;
import com.logitrack.courier_service.dominio.enumerador.EstadoRepartidor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // <-- ESTE ES EL CORRECTO

import java.util.UUID;

public interface RepartidorServicio {
    RepartidorResponseDto registrarRepartidor(RepartidorRequestDto request);
    RepartidorResponseDto obtenerPorId(UUID id);
    Page<RepartidorResponseDto> listarRepartidores(EstadoRepartidor estado, Pageable pageable);
    RepartidorResponseDto actualizarRepartidor(UUID id, RepartidorRequestDto request);
    void cambiarEstado(UUID id, EstadoRepartidor nuevoEstado);
    RepartidorResponseDto obtenerDisponibilidad(UUID id);
    void eliminarRepartidor(UUID id);
}
