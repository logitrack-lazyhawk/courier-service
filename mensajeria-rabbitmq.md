# F08 — Configuración de mensajería RabbitMQ

## Objetivo
Implementar la infraestructura de mensajería asíncrona para la comunicación entre `shipment-service` y `courier-service` de LogiTrack, mediante RabbitMQ.

## Arquitectura de mensajería
Se define un exchange de tipo `topic` denominado `logitrack.assignment.events`, que permite distribuir eventos utilizando routing keys.

### Colas y routing keys

| Cola | Routing key |
|---|---|
| courier.assignment.requests.q | shipment.assignment.requested.v1 |
| shipment.assignment.results.q | courier.assignment.*.v1 |

El exchange y ambas colas son durables.

## Contratos de eventos

**AsignacionSolicitadaV1**
- eventoId
- solicitudId
- version
- ocurridoEn
- envioId
- repartidorId

**AsignacionResultadoV1**
- eventoId
- solicitudId
- version
- ocurridoEn
- envioId
- repartidorId
- aceptada
- motivo

Los eventos utilizan una versión inicial `v1` para facilitar la evolución de los contratos.

## Implementación
Se incorporó Spring AMQP y se creó la clase `RabbitMqConfig`, responsable de definir el exchange, las colas y los bindings.

## Validación
Se verificó en RabbitMQ Management la existencia del exchange de tipo `topic`, las colas durables y sus bindings.

Las pruebas automatizadas comprueban la configuración de los recursos de mensajería.

## Alcance
Esta feature establece la infraestructura y los contratos. La publicación, el consumo de eventos y los mecanismos de recuperación se implementarán en las features posteriores del Sprint 4.

## F10 — Consumo de eventos de asignación

Courier consume eventos `AsignacionSolicitadaV1` desde RabbitMQ
mediante la cola `courier.assignment.requests.q`.

### Componentes
- ConsumidorAsignacionRabbitMq: recibe los eventos.
- ProcesadorSolicitudAsignacion: valida la solicitud.
- RepartidorServicio: consulta la disponibilidad del repartidor.

### Reglas de negocio
- Repartidor ACTIVO: solicitud aceptada.
- Repartidor INACTIVO: solicitud rechazada.
- Repartidor inexistente: solicitud rechazada.
- Evento inválido: error de procesamiento.

El procesamiento genera un AsignacionResultadoV1.

### Pruebas
- 29 pruebas automatizadas exitosas.
- Prueba de integración con RabbitMQ exitosa.
- Conversión JSON y recepción real verificadas.

### Pendiente
La publicación del resultado hacia Shipment corresponde a F11.
Los reintentos, DLQ e idempotencia corresponden a F12.

## F36 — Pruebas de contratos y consistencia de eventos

Se incorporaron pruebas para validar el intercambio de eventos V1 entre Shipment y Courier y su comportamiento frente a entregas duplicadas y mensajes inválidos.

### Cobertura

- **Contratos:** serialización y deserialización de `AsignacionSolicitadaV1` y `AsignacionResultadoV1`.
- **Idempotencia:** una misma `solicitudId` genera una única decisión persistida, aun cuando se reciba más de una vez.
- **DLQ:** un evento con versión incompatible se rechaza y RabbitMQ lo dirige a la cola de mensajes fallidos.
- **Durabilidad:** se comprueba la persistencia de un mensaje después del reinicio real del broker.

### Estrategia de pruebas

Las pruebas unitarias utilizan JUnit y Mockito. Las pruebas de integración utilizan H2 y una única instancia de RabbitMQ, evitando desplegar todo el ecosistema.

La prueba de reinicio utiliza una cola durable aislada y un mensaje persistente.

### Limitaciones

La estrategia garantiza procesamiento idempotente de solicitudes, pero no implica entrega exactamente una vez (*exactly-once delivery*).

La prueba de reinicio comprueba la persistencia en el broker, sin demostrar por sí sola la recuperación completa del flujo distribuido.

### Evidencia

Agregar los resultados de ejecución de Maven, recepción en DLQ y recuperación del mensaje tras reiniciar RabbitMQ. Registrar los casos como exitosos únicamente después de verificarlos.

t1@mx:~/Desktop/DAAS/logitrack-infra
$
curl -fsS \
-u "$RABBITMQ_USER:$RABBITMQ_PASSWORD" \
-H "Content-Type: application/json" \
-X POST \
http://127.0.0.1:15673/api/exchanges/%2F/amq.default/publish \
-d '{"properties":{"delivery_mode":2},"routing_key":"f36.restart.q","payload":"F36-reinicio-broker","payload_encoding":"string"}'
{"routed":true}t1@mx:~/Desktop/DAAS/logitrack-infra
$
docker compose restart rabbitmq
docker compose ps rabbitmq
[+] Restarting 1/1
✔ Container logitrack-rabbitmq  Started                                                                                                                                       0.9s
NAME                 IMAGE                              COMMAND                  SERVICE    CREATED       STATUS                                     PORTS
logitrack-rabbitmq   rabbitmq:4.4.0-management-alpine   "docker-entrypoint.s…"   rabbitmq   4 hours ago   Up Less than a second (health: starting)   4369/tcp, 5671/tcp, 15671/tcp, 15691-15692/tcp, 25672/tcp, 127.0.0.1:5673->5672/tcp, 127.0.0.1:15673->15672/tcp
t1@mx:~/Desktop/DAAS/logitrack-infra
$
curl --retry 15 --retry-delay 2 --retry-connrefused -fsS \
-u "$RABBITMQ_USER:$RABBITMQ_PASSWORD" \
-H "Content-Type: application/json" \
-X POST \
http://127.0.0.1:15673/api/queues/%2F/f36.restart.q/get \
-d '{"count":1,"ackmode":"ack_requeue_false","encoding":"auto"}'
[{"payload_bytes":19,"redelivered":false,"exchange":"","routing_key":"f36.restart.q","message_count":0,"properties":{"delivery_mode":2},"payload":"F36-reinicio-broker","payload_encoding":"string"}]t1@mx:~/Desktop/DAAS/logitrack-infra

[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 11.63 s -- in com.logitrack.courier_service.mensajeria.consumidor.RabbitMqConsumoIT
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  18.112 s
[INFO] Finished at: 2026-11-10T15:36:24-03:00
[INFO] ------------------------------------------------------------------------