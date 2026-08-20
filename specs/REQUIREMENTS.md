# REQUIREMENTS.md - Fulvis

**Estado:** Fase 1  
**Ultima revision:** 2026-08-17

---

## Seccion 1 - Gestion de Stock

**REQ-S01 - Consultar stock**
El sistema debe permitir consultar el `stock_total`, `stock_reserved` y `stock_available` de un producto por su identificador, independientemente de si tiene pedidos activos asociados.

**REQ-S02 - Reservar stock**
Al crear un pedido, el sistema debe incrementar `stock_reserved` en la cantidad solicitada. Si `stock_available` es insuficiente, el pedido no se crea y el stock no se modifica. Ambas operaciones son atomicas y se ejecutan en la misma transaccion. Ver ADR-001.

**REQ-S03 - Liberar stock**
Al cancelar un pedido, el sistema debe decrementar `stock_reserved` en la cantidad del pedido. Esta operacion se ejecuta en la misma transaccion que el cambio de estado a CANCELLED.

**REQ-S04 - Decrementar stock**
Al transicionar un pedido a SHIPPED, el sistema debe decrementar `stock_total` y `stock_reserved` en la cantidad del pedido. Ambos decrementos se ejecutan en la misma transaccion que el cambio de estado.

---

## Seccion 2 - Ciclo de vida del pedido

**REQ-P01 - Crear pedido**
El sistema debe crear un pedido en estado MANAGED y reservar el stock solicitado de forma atomica. Si el stock disponible es insuficiente, devuelve error de negocio y no crea el pedido ni modifica el stock.

**REQ-P02 - Iniciar preparacion**
El operador puede transicionar un pedido de MANAGED a IN_PREPARATION mediante llamada explicita a la API. No se revalida stock en esta transicion.

**REQ-P03 - Marcar como enviado**
El operador puede transicionar un pedido de IN_PREPARATION a SHIPPED mediante llamada explicita a la API. En esta transicion el stock se decrementa permanentemente. A partir de este estado el pedido no puede cancelarse.

**REQ-P04 - Confirmar recepcion**
El operador puede transicionar un pedido de SHIPPED a RECEIVED mediante llamada explicita a la API. Esta transicion no produce cambios en stock.

**REQ-P05 - Cerrar pedido**
El operador puede cerrar manualmente un pedido en estado RECEIVED mediante llamada explicita a la API. El sistema transiciona a CLOSED. No produce cambios en stock.

**REQ-P06 - Cancelar pedido**
El operador puede cancelar un pedido en estado MANAGED o IN_PREPARATION. El stock reservado se libera en la misma transaccion que el cambio de estado a CANCELLED. No es posible cancelar desde SHIPPED, RECEIVED o CLOSED.

---

## Seccion 3 - Reglas de negocio

**RN-01** `stock_available = stock_total - stock_reserved`. Es un valor derivado, no almacenado.

**RN-02** `stock_total` y `stock_reserved` nunca pueden ser negativos.

**RN-03** `stock_total >= stock_reserved` en todo momento. Una reserva nunca puede superar el total fisico.

**RN-04** Un pedido solo puede estar en un estado a la vez. Las transiciones son hacia adelante unicamente; no existe retroceso de estado.

**RN-05** Las unicas transiciones operativas validas sobre un pedido existente son las definidas en REQ-P02 a REQ-P06. La creacion de pedido definida en REQ-P01 construye un nuevo `Order` en estado `MANAGED`, pero no forma parte del `OrderStateMachine` operativo.

---

## Seccion 4 - Requisitos no funcionales

**NFR-IDP-01 - Idempotencia**
El sistema debe garantizar que los reintentos de peticiones duplicadas -originados por fallos de red o acciones del usuario- no alteren el estado del sistema mas alla de la primera ejecucion exitosa, devolviendo siempre la misma respuesta confirmada. Ver ADR-002.

**NFR-CON-01 - Consistencia**
La consistencia de los datos entre pedidos y stock es un requisito critico. Ninguna operacion puede dejar el sistema en un estado donde un pedido y su stock asociado sean inconsistentes entre si.

**NFR-OBS-01 - Trazabilidad operacional**
Todas las operaciones deben incluir un identificador de traza (`traceId`) para permitir el seguimiento y diagnostico de errores en los flujos internos.

**NFR-AUD-01 - Auditabilidad minima**
Cada transicion de estado de un pedido debe persistir en una tabla de historial dentro de la misma transaccion de base de datos: identificador del pedido, estado anterior, estado nuevo y timestamp del cambio. Si la escritura del registro falla, la transaccion completa se revierte.

---

## Seccion 5 - Restricciones tecnicas

**RT-01 - Limite transaccional**
En Fase 1, todas las operaciones que afecten conjuntamente a `Order` y `Stock` se ejecutan dentro de una unica transaccion de base de datos local. No se requieren patrones de consistencia distribuida.

**RT-02 - API**
La comunicacion se realiza mediante API REST sincrona. El contrato se define con OpenAPI siguiendo el principio API-First.

**RT-03 - Arquitectura**
El sistema se estructura siguiendo arquitectura hexagonal para facilitar el aprendizaje, la comprension de limites entre capas y la evaluacion critica del codigo generado.

**RT-04 - Stack obligatorio**
Java 21, Spring Boot, Maven, PostgreSQL, Docker, JUnit, Mockito. Testcontainers donde aporte valor en tests de integracion.

**RT-05 - Testing**
Se aplica la piramide de testing: tests unitarios sobre logica de dominio, tests de integracion sobre puertos y adaptadores, tests end-to-end sobre flujos completos. Formato Given-When-Then.
