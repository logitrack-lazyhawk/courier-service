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

## Capturas 
