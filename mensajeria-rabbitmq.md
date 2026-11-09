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