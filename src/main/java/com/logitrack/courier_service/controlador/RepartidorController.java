package com.logitrack.courier_service.controlador;

import com.logitrack.courier_service.dominio.dto.RepartidorRequestDto;
import com.logitrack.courier_service.dominio.dto.RepartidorResponseDto;
import com.logitrack.courier_service.dominio.enumerador.EstadoRepartidor;
import com.logitrack.courier_service.servicio.RepartidorServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/repartidores")
@RequiredArgsConstructor
public class RepartidorController {

    private final RepartidorServicio repartidorServicio;

    @PostMapping
    public ResponseEntity<RepartidorResponseDto> registrar(@Valid @RequestBody RepartidorRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repartidorServicio.registrarRepartidor(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepartidorResponseDto> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(repartidorServicio.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<RepartidorResponseDto>> listar(@RequestParam(required = false) EstadoRepartidor estado) {
        return ResponseEntity.ok(repartidorServicio.listarRepartidores(estado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RepartidorResponseDto> actualizar(@PathVariable UUID id, @Valid @RequestBody RepartidorRequestDto request) {
        return ResponseEntity.ok(repartidorServicio.actualizarRepartidor(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable UUID id, @RequestParam EstadoRepartidor estado) {
        repartidorServicio.cambiarEstado(id, estado);
        return ResponseEntity.noContent().build();
    }

    // Endpoint clave para el Sprint 2 (lo consumirá el shipment-service)
    @GetMapping("/{id}/disponibilidad")
    public ResponseEntity<RepartidorResponseDto> consultarDisponibilidad(@PathVariable UUID id) {
        return ResponseEntity.ok(repartidorServicio.obtenerDisponibilidad(id));
    }
}
