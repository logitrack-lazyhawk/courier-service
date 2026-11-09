package com.logitrack.courier_service.dominio.entidad;

import com.logitrack.courier_service.mensajeria.contrato.AsignacionResultadoV1;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionSolicitadaV1;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Inbox durable: una sola decisión de asignación por solicitudId. */
@Entity
@Table(name = "asignaciones_procesadas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AsignacionProcesada {

    @Id
    @Column(name = "solicitud_id", nullable = false, updatable = false, columnDefinition = "binary(16)")
    private UUID solicitudId;

    @Column(name = "evento_solicitud_id", nullable = false, updatable = false, columnDefinition = "binary(16)")
    private UUID eventoSolicitudId;

    @Column(name = "evento_resultado_id", nullable = false, updatable = false, columnDefinition = "binary(16)")
    private UUID eventoResultadoId;

    @Column(name = "envio_id", nullable = false, updatable = false, columnDefinition = "binary(16)")
    private UUID envioId;

    @Column(name = "repartidor_id", nullable = false, updatable = false, columnDefinition = "binary(16)")
    private UUID repartidorId;

    @Column(name = "aceptada", nullable = false, updatable = false)
    private boolean aceptada;

    @Column(name = "motivo", length = 1024, updatable = false)
    private String motivo;

    @Column(name = "ocurrido_en", nullable = false, updatable = false)
    private Instant ocurridoEn;

    public static AsignacionProcesada desde(
            AsignacionSolicitadaV1 solicitud, AsignacionResultadoV1 resultado) {
        AsignacionProcesada entity = new AsignacionProcesada();
        entity.solicitudId = solicitud.solicitudId();
        entity.eventoSolicitudId = solicitud.eventoId();
        entity.eventoResultadoId = resultado.eventoId();
        entity.envioId = solicitud.envioId();
        entity.repartidorId = solicitud.repartidorId();
        entity.aceptada = resultado.aceptada();
        entity.motivo = resultado.motivo();
        entity.ocurridoEn = resultado.ocurridoEn();
        return entity;
    }

    public AsignacionResultadoV1 mismoResultadoPara(AsignacionSolicitadaV1 solicitud) {
        if (!envioId.equals(solicitud.envioId())
                || !repartidorId.equals(solicitud.repartidorId())) {
            throw new IllegalArgumentException(
                    "La solicitudId ya existe con un envío o repartidor diferente.");
        }
        return new AsignacionResultadoV1(
                eventoResultadoId,
                solicitudId,
                1,
                ocurridoEn,
                envioId,
                repartidorId,
                aceptada,
                motivo);
    }
}
