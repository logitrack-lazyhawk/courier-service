package com.logitrack.courier_service.servicio;

import com.logitrack.courier_service.dominio.dto.RepartidorRequestDto;
import com.logitrack.courier_service.dominio.dto.RepartidorResponseDto;
import com.logitrack.courier_service.dominio.enumerador.EstadoRepartidor;

import java.util.List;
import java.util.UUID;

public interface RepartidorServicio {
    RepartidorResponseDto registrarRepartidor(RepartidorRequestDto request);
    RepartidorResponseDto obtenerPorId(UUID id);
    List<RepartidorResponseDto> listarRepartidores(EstadoRepartidor estado);
    RepartidorResponseDto actualizarRepartidor(UUID id, RepartidorRequestDto request);
    void cambiarEstado(UUID id, EstadoRepartidor nuevoEstado);
    RepartidorResponseDto obtenerDisponibilidad(UUID id); // Para el shipment-service
}
