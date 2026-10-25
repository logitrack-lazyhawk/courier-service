package com.logitrack.curier_service.repositorio;

import com.logitrack.curier_service.dominio.entidad.Repartidor;
import com.logitrack.curier_service.dominio.enumerador.EstadoRepartidor;
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
    List<Repartidor> findByEstado(EstadoRepartidor estado);

    // Para validar si existe un DNI antes de registrar
    boolean existsByDni(String dni);
}
