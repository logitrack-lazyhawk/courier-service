package com.logitrack.courier_service.repositorio;

import com.logitrack.courier_service.dominio.entidad.AsignacionProcesada;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsignacionProcesadaRepository extends JpaRepository<AsignacionProcesada, UUID> {
}
