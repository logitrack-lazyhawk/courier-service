# Courier-Service

Microservicio especializado de la plataforma **LazyHawk** para la gestión del personal de reparto (couriers), control de flota, validación de disponibilidad en tiempo real y soporte al ciclo de última milla.

---

## 📌 Descripción General

Dentro de la arquitectura orientada a microservicios de LazyHawk, courier-service desacopla completamente el dominio de los repartidores de la gestión transaccional de envíos (shipment-service).  
Proporciona operaciones CRUD, normalización de formatos de identidad y contacto (DNI y teléfono internacional E.164), filtrado de disponibilidad operativa (ACTIVO / INACTIVO) y persistencia con soft delete.

---

## 🚀 Responsabilidades y Endpoints REST

La API base se encuentra bajo /api/v1/repartidores.

| Método | Endpoint | Descripción |
|---|---|---|
| POST | /api/v1/repartidores | Registra un nuevo repartidor con normalización y validaciones |
| GET | /api/v1/repartidores/{id} | Consulta el detalle y disponibilidad de un repartidor por UUID |
| GET | /api/v1/repartidores | Listado paginado de repartidores con filtro opcional por estado |
| PUT | /api/v1/repartidores/{id} | Actualización completa de datos del repartidor |
| DELETE | /api/v1/repartidores/{id} | Baja lógica (Soft Delete automático por timestamp) |

---

## 🛠️ Stack Tecnológico

| Tecnología | Descripción / Uso |
|---|---|
| **Java 25** | Lenguaje de programación |
| **Spring Boot 4.x** | Framework base para desarrollo de microservicios |
| **Spring Data JPA & Hibernate** | Persistencia y mapeo objeto-relacional |
| **Spring Cloud Config Client** | Centralización de configuraciones dinámicas desde configserver-ms |
| **MySQL 8** | Base de datos relacional para entornos de desarrollo y producción |
| **H2 Database** | Base de datos en memoria para testing aislado |
| **Springdoc OpenAPI (Swagger)** | Documentación interactiva de endpoints (/swagger-ui.html) |
| **JUnit 5 & Mockito** | Pruebas unitarias y de integración |
| **Docker** | Empaquetado en contenedor con Dockerfile multi-etapa |

---

## 👤 Modelo de Dominio (Repartidor)

La entidad Repartidor hereda de EntidadAuditable y contiene:
- id: Identificador único UUID generado automáticamente.
- 
ombre: Nombre y apellido del repartidor.
- dni: Documento civil único (7 u 8 dígitos numéricos).
- email: Correo electrónico con validación de formato.
- 	elefono: Número de contacto normalizado al estándar internacional E.164 (+54...).
- 	ipoVehiculo: Medio de transporte (MOTO, BICICLETA, AUTO, CAMIONETA).
- estado: Estado operativo (ACTIVO, INACTIVO).
- echaCreacion / echaModificacion: Marcas de auditoría.
- echaEliminacion: Timestamp de soft delete.

---

## 🌐 Configuración Centralizada y Variables de Entorno

courier-service se integra con Spring Cloud Config (configserver-ms):

`yaml
spring:
  config:
    import: "optional:configserver:\"
  profiles:
    active: \
`

### Variables de Entorno:
- CONFIG_SERVER_URL: URL del servidor de configuración central (http://config-server:8085).
- SPRING_PROFILES_ACTIVE: Perfil de ejecución (dev o prod).
- DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD: Parámetros de la base de datos courier_db.

---

## 🧪 Pruebas y Calidad

Para ejecutar la suite de pruebas unitarias y de integración:

`ash
./mvnw clean test
`

---

## 👥 Equipo y Autores
* **Mamani, Luis Carlos Manuel** - [LuisMPlus](https://github.com/LuisMPlus)
* **Maygua Altamirano, Pio Roman** - [95670446-maker](https://github.com/95670446-maker)
* **Mamani Leandro** - [leoM2022](https://github.com/leoM2022)

LazyHawk · Desarrollo y Arquitecturas Avanzadas de Software