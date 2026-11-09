package com.logitrack.courier_service.servicio;

import com.logitrack.courier_service.dominio.entidad.AsignacionProcesada;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionResultadoV1;
import com.logitrack.courier_service.mensajeria.contrato.AsignacionSolicitadaV1;
import com.logitrack.courier_service.repositorio.AsignacionProcesadaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Persiste el resultado antes de publicarlo; los reintentos reutilizan esa decisión. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcesadorIdempotenteAsignacion {

    private final AsignacionProcesadaRepository repositorio;
    private final ProcesadorSolicitudAsignacion procesador;
    private final PlatformTransactionManager transactionManager;

    public AsignacionResultadoV1 procesar(AsignacionSolicitadaV1 evento) {
        validar(evento);
        TransactionTemplate transaccion = new TransactionTemplate(transactionManager);

        try {
            return transaccion.execute(estado -> repositorio.findById(evento.solicitudId())
                    .map(anterior -> {
                        log.info("Solicitud {} repetida: se reutiliza resultado", evento.solicitudId());
                        return anterior.mismoResultadoPara(evento);
                    })
                    .orElseGet(() -> {
                        AsignacionResultadoV1 resultado = procesador.procesar(evento);
                        repositorio.saveAndFlush(AsignacionProcesada.desde(evento, resultado));
                        return resultado;
                    }));
        } catch (DataIntegrityViolationException ex) {
            // Dos consumidores pudieron competir por la misma PK; la transacción
            // fallida ya se revirtió. Se lee el registro ganador en una transacción nueva.
            return transaccion.execute(estado -> repositorio.findById(evento.solicitudId())
                    .map(anterior -> anterior.mismoResultadoPara(evento))
                    .orElseThrow(() -> ex));
        }
    }

    private void validar(AsignacionSolicitadaV1 evento) {
        if (evento == null || evento.eventoId() == null
                || evento.solicitudId() == null || evento.version() != 1
                || evento.ocurridoEn() == null || evento.envioId() == null
                || evento.repartidorId() == null) {
            throw new IllegalArgumentException("Evento de asignación inválido o incompatible.");
        }
    }
}
