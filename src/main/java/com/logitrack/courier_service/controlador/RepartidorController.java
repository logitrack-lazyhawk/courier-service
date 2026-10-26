package com.logitrack.courier_service.controlador;

import com.logitrack.courier_service.dominio.dto.RepartidorRequestDto;
import com.logitrack.courier_service.dominio.dto.RepartidorResponseDto;
import com.logitrack.courier_service.dominio.enumerador.EstadoRepartidor;
import com.logitrack.courier_service.servicio.RepartidorServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/repartidores")
@RequiredArgsConstructor
@Tag(name = "Gestión de Repartidores", description = "CRUD y validación de disponibilidad de repartidores")
public class RepartidorController {

    private final RepartidorServicio repartidorServicio;

    @Operation(summary = "Registrar un nuevo repartidor")
    @PostMapping
    public ResponseEntity<RepartidorResponseDto> registrar(@Valid @RequestBody RepartidorRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repartidorServicio.registrarRepartidor(request));
    }

    @Operation(summary = "Obtener un repartidor por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<RepartidorResponseDto> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(repartidorServicio.obtenerPorId(id));
    }

    @Operation(summary = "Listar repartidores paginados (filtro opcional por estado)")
    @GetMapping
    public ResponseEntity<Page<RepartidorResponseDto>> listar(
            @RequestParam(required = false) EstadoRepartidor estado,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(repartidorServicio.listarRepartidores(estado, pageable));
    }

    @Operation(summary = "Actualizar los datos de un repartidor")
    @PutMapping("/{id}")
    public ResponseEntity<RepartidorResponseDto> actualizar(@PathVariable UUID id, @Valid @RequestBody RepartidorRequestDto request) {
        return ResponseEntity.ok(repartidorServicio.actualizarRepartidor(id, request));
    }

    @Operation(summary = "Cambiar el estado operativo (ACTIVO/INACTIVO)")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable UUID id, @RequestParam EstadoRepartidor estado) {
        repartidorServicio.cambiarEstado(id, estado);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Eliminar (baja lógica) un repartidor")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        repartidorServicio.eliminarRepartidor(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Consultar disponibilidad (consumido por shipment-service)")
    @GetMapping("/{id}/disponibilidad")
    public ResponseEntity<RepartidorResponseDto> consultarDisponibilidad(@PathVariable UUID id) {
        return ResponseEntity.ok(repartidorServicio.obtenerDisponibilidad(id));
    }
}
