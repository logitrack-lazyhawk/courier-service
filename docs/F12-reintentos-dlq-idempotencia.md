# F12 — Reintentos, DLQ e idempotencia (courier-service)

## Flujo
1. `shipment.assignment.requested.v1` entra a `courier.assignment.requests.q`.
2. `ProcesadorIdempotenteAsignacion` valida `solicitudId`, consulta MySQL y persiste una única decisión en `asignaciones_procesadas` (PK `solicitud_id`).
3. El publicador envía la confirmación/rechazo a `logitrack.assignment.events`, espera el confirm ACK del broker y verifica que el mensaje no haya sido retornado sin destinatario.
4. Solo al terminar exitosamente el listener se reconoce el mensaje de entrada (ACK automático del contenedor).
5. Los errores provocan hasta 3 intentos locales con backoff de 500 ms a 1 s; al agotarse se rechaza el mensaje sin reencolar y RabbitMQ lo pasa a `courier.assignment.requests.dlq` mediante `logitrack.assignment.dlx`.

## Notas de consistencia
- Reentregas de la misma solicitud siempre devuelven el **mismo `eventoResultadoId` y resultado** almacenados, incluso si cambió luego el estado del repartidor.
- Un `solicitudId` reutilizado para otro envío o repartidor se rechaza (va a DLQ).
- Dos consumidores simultáneos compiten por la PK en MySQL; el perdedor, tras revertir su transacción, lee el registro ganador.
- No se promete *exactly-once delivery*. Se admite que un resultado se publique más de una vez si el proceso cae después del confirm del broker pero antes del ACK de la solicitud; `shipment-service` ya posee lógica para ignorar el mismo resultado repetido.
- En `shipment-service`, la publicación de solicitudes `AFTER_COMMIT` registra errores sin persistir un outbox; eso es una limitación previa ajena a este issue.

## Requisitos de RabbitMQ
- Publisher confirms correlacionados, publisher returns y mandatory activados en `application.yaml`.
- DLX: `logitrack.assignment.dlx` (durable topic).
- DLQ: `courier.assignment.requests.dlq` (durable).
- Routing de DLQ: `courier.assignment.requests.dead`.
- La cola original `courier.assignment.requests.q` ya existe sin los nuevos argumentos DLX: RabbitMQ **rechaza redeclararla** si se intenta modificar en caliente. **No la borre si tiene mensajes pendientes.** Antes de la integración, compruebe que está vacía, detenga consumidores, elimine SOLO ESA cola desde RabbitMQ Management y permita su recreación con los nuevos argumentos. Alternativamente use una policy del broker que coincida con la configuración; no combine policy y argumentos contradictorios.
- No publique manualmente en la DLQ mientras no comprenda el contenido. Revisar `x-death`, causa y número de entregas; devolver a cola principal de forma controlada tras corregir el problema.

## MySQL
- Entidad `AsignacionProcesada` y repositorio JPA nuevos.
- Si Hibernate utiliza `ddl-auto=update`, la tabla puede crearse automáticamente al arrancar. Para `validate`/`none`, aplicar `docs/F12-migracion-mysql.sql` a la BD específica de courier-service.

## Pruebas livianas (sin Docker)
`./mvnw -Dtest=RabbitMqConfigTest,ConsumidorAsignacionRabbitMqTest,ProcesadorIdempotenteAsignacionTest,PublicadorResultadoAsignacionRabbitMqTest test`

## Evidencias finales (con broker y DB)
- Un evento válido confirma/rechaza y se registra en MySQL.
- Duplicar evento/solicitud: una sola decisión persistida, resultado estable.
- Provocar publicación fallida con el broker no disponible: 3 reintentos y mensaje en DLQ, sin perder la decisión en DB.
- Corregir falla, reprocesar manualmente: se reutiliza el mismo resultado.
- Validar que `shipment-service` no altera dos veces el mismo envío.
