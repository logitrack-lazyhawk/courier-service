package com.logitrack.courier_service.servicio.impl;

import com.logitrack.courier_service.dominio.dto.RepartidorRequestDto;
import com.logitrack.courier_service.dominio.dto.RepartidorResponseDto;
import com.logitrack.courier_service.dominio.entidad.Repartidor;
import com.logitrack.courier_service.dominio.enumerador.EstadoRepartidor;
import com.logitrack.courier_service.repositorio.RepartidorRepository;
import com.logitrack.courier_service.servicio.RepartidorServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RepartidorServicioImpl implements RepartidorServicio {

    private final RepartidorRepository repartidorRepository;

    @Override
    @Transactional
    public RepartidorResponseDto registrarRepartidor(RepartidorRequestDto request) {
        if (repartidorRepository.existsByDni(request.dni())) {
            throw new IllegalArgumentException("Ya existe un repartidor con el DNI proporcionado.");
        }

        Repartidor repartidor = new Repartidor();
        repartidor.setNombre(request.nombre());
        repartidor.setDni(request.dni());
        repartidor.setEmail(request.email());
        repartidor.setTelefono(request.telefono());
        repartidor.setTipoVehiculo(request.tipoVehiculo());
        repartidor.setEstado(request.estado());

        Repartidor guardado = repartidorRepository.save(repartidor);
        return mapearHaciaResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public RepartidorResponseDto obtenerPorId(UUID id) {
        Repartidor repartidor = repartidorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Repartidor no encontrado con ID: " + id));
        return mapearHaciaResponse(repartidor);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RepartidorResponseDto> listarRepartidores(EstadoRepartidor estado, Pageable pageable) {
        Page<Repartidor> pagina = (estado != null)
                ? repartidorRepository.findByEstado(estado, pageable)
                : repartidorRepository.findAll(pageable);

        return pagina.map(this::mapearHaciaResponse);
    }

    @Override
    @Transactional
    public RepartidorResponseDto actualizarRepartidor(UUID id, RepartidorRequestDto request) {
        Repartidor repartidor = repartidorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Repartidor no encontrado con ID: " + id));

        // Si cambia el DNI, validamos que no exista otro con ese DNI
        if (!repartidor.getDni().equals(request.dni()) && repartidorRepository.existsByDni(request.dni())) {
            throw new IllegalArgumentException("El DNI ya está registrado por otro repartidor.");
        }

        repartidor.setNombre(request.nombre());
        repartidor.setDni(request.dni());
        repartidor.setEmail(request.email());
        repartidor.setTelefono(request.telefono());
        repartidor.setTipoVehiculo(request.tipoVehiculo());
        repartidor.setEstado(request.estado());

        return mapearHaciaResponse(repartidorRepository.save(repartidor));
    }

    @Override
    @Transactional
    public void cambiarEstado(UUID id, EstadoRepartidor nuevoEstado) {
        Repartidor repartidor = repartidorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Repartidor no encontrado con ID: " + id));

        repartidor.setEstado(nuevoEstado);
        repartidorRepository.save(repartidor);
    }

    @Override
    @Transactional(readOnly = true)
    public RepartidorResponseDto obtenerDisponibilidad(UUID id) {
        // En el Sprint 2, este endpoint será consumido por el shipment-service
        Repartidor repartidor = repartidorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Repartidor no encontrado con ID: " + id));

        // Si está INACTIVO, lanzamos excepción para que el shipment-service sepa que no puede asignarlo
        if (repartidor.getEstado() == EstadoRepartidor.INACTIVO) {
            throw new IllegalArgumentException("El repartidor se encuentra INACTIVO y no está disponible.");
        }

        return mapearHaciaResponse(repartidor);
    }

    private RepartidorResponseDto mapearHaciaResponse(Repartidor repartidor) {
        return new RepartidorResponseDto(
                repartidor.getId(),
                repartidor.getNombre(),
                repartidor.getDni(),
                repartidor.getEmail(),
                repartidor.getTelefono(),
                repartidor.getTipoVehiculo(),
                repartidor.getEstado()
        );
    }
    @Override
    @Transactional
    public void eliminarRepartidor(UUID id) {
        if (!repartidorRepository.existsById(id)) {
            throw new IllegalArgumentException("Repartidor no encontrado con ID: " + id);
        }
        repartidorRepository.deleteById(id);
    }
}
