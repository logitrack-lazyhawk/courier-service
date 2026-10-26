package com.logitrack.courier_service.repositorio;

import com.logitrack.courier_service.dominio.entidad.Repartidor;
import com.logitrack.courier_service.dominio.enumerador.EstadoRepartidor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepartidorRepository extends JpaRepository<Repartidor, UUID> {
    // Para buscar por DNI (útil para validaciones)
    Optional<Repartidor> findByDni(String dni);

    // Para listar repartidores por estado (ACTIVO/INACTIVO)
    Page<Repartidor> findByEstado(EstadoRepartidor estado, Pageable pageable);

    // Para validar si existe un DNI antes de registrar
    boolean existsByDni(String dni);

}
