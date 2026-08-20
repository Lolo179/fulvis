# APPLICATION.md - Fulvis

**Estado:** Fase 1  
**Ultima revision:** 2026-08-18

---

## Proposito

Este documento describe la capa de aplicacion de Fulvis: casos de uso, orquestacion entre agregados, limites transaccionales y dependencias hacia puertos.

La capa de aplicacion no contiene reglas de negocio profundas. Su responsabilidad es coordinar agregados y servicios de dominio en el orden correcto.

---

## Principios

- Los casos de uso dependen de puertos, no de adaptadores concretos.
- Los casos de uso declaran el limite transaccional de la operacion.
- Las reglas internas de `Order` viven en el agregado `Order`.
- Las invariantes de stock viven en el agregado `Item`.
- La politica de transiciones vive en `OrderStateMachine`.
- La capa de aplicacion coordina; no decide reglas de transicion ni valida invariantes internas de agregados.

---

## Puertos requeridos

### OrderRepositoryPort

Responsable de cargar y persistir agregados `Order`.

Operaciones conceptuales:

```text
findById(orderId)
save(order)
```

### ItemRepositoryPort

Responsable de cargar y persistir agregados `Item`.

Para operaciones que modifican stock, la carga debe respetar la estrategia de concurrencia definida en `ADR-001`.

Operaciones conceptuales:

```text
findAllByIdsForUpdate(itemIds)
saveAll(items)
```

### Clock

Fuente de tiempo para generar timestamps de dominio como `createdAt`, `updatedAt` y entradas de `OrderHistory`.

Se prefiere `java.time.Clock` frente a crear un puerto propio en Fase 1.

---

## Contratos de entrada y salida

Los contratos de aplicacion representan la entrada y salida de los casos de uso. No son DTOs REST, aunque la API pueda mapearse hacia ellos.

Esta separacion permite probar casos de uso desde tests, harnesses, CLI, API REST o agentes sin acoplar la logica de aplicacion al transporte.

### CreateOrderCommand

```text
lines: lista de CreateOrderLineCommand
```

### CreateOrderLineCommand

```text
itemId
quantity
```

### CreateOrderResult

```text
orderId
status
createdAt
```

### TransitionOrderCommand

```text
orderId
event
```

### TransitionOrderResult

```text
orderId
status
occurredAt
idempotent
```

`idempotent` indica si el caso de uso devolvio exito porque la operacion ya estaba satisfecha. Este dato pertenece al resultado de aplicacion para facilitar pruebas, harnesses y observabilidad interna; no implica que deba exponerse necesariamente en el contrato REST publico.

---

## Errores conceptuales

Los errores deben nombrarse explicitamente para que el comportamiento del sistema sea verificable y mapeable por adaptadores externos.

Errores iniciales:

```text
OrderNotFound
ItemNotFound
InvalidOrderTransition
InsufficientStock
InvalidOrderLines
ConcurrencyConflict
```

Notas:

- `InvalidOrderTransition` representa eventos no validos para el estado actual del pedido.
- `InsufficientStock` pertenece a invariantes de `Item`.
- `InvalidOrderLines` representa errores como cantidades invalidas o lineas duplicadas por `itemId`.
- `ConcurrencyConflict` solo debe propagarse si, tras aplicar la regla de idempotencia de `ADR-002`, el estado actual no satisface la operacion solicitada.

### Clasificacion por responsabilidad

| Error | Capa que lo produce | Responsable conceptual | Comentario |
|---|---|---|---|
| `InvalidOrderTransition` | Dominio | `OrderStateMachine` / `Order` | La politica de transicion y las guardas del pedido determinan si el evento es valido |
| `InsufficientStock` | Dominio | `Item` | Solo `Item` conoce y protege `stockAvailable` |
| `InvalidOrderLines` | Dominio | `Order` | Lineas vacias, cantidades invalidas o `itemId` duplicado violan invariantes del pedido |
| `OrderNotFound` | Aplicacion | Caso de uso | La ausencia se detecta al cargar el agregado por ID |
| `ItemNotFound` | Aplicacion | Caso de uso | El caso de uso no puede completar la orquestacion si faltan items referenciados |
| `ConcurrencyConflict` | Aplicacion / Persistencia traducida | Caso de uso | Nace en persistencia, pero la aplicacion decide si se resuelve como idempotente o si se propaga |

Regla de diseno:

Los adaptadores externos no crean decisiones de negocio. Solo traducen comandos, resultados y errores hacia el protocolo correspondiente, por ejemplo HTTP.

---

## CreateOrderUseCase

Construye un nuevo `Order` en estado inicial `MANAGED` y reserva stock en la misma transaccion.

Secuencia conceptual:

```text
1. Recibir datos de creacion del pedido
2. Validar la solicitud minima de entrada
3. Verificar que no existan lineas duplicadas por itemId
4. Cargar Items por itemId con lock pesimista
5. Ejecutar item.reserve(quantity) por cada linea
6. Crear Order en estado MANAGED
7. Crear historial inicial null -> MANAGED
8. Persistir Order e Items dentro de la misma transaccion
```

Notas:

- La reserva de stock usa locking pesimista segun `ADR-001`.
- La creacion no usa `OrderStateMachine`, porque todavia no existe un agregado `Order` sobre el que transicionar.
- El historial inicial forma parte del agregado `Order`.

---

## TransitionOrderUseCase

Aplica un evento de negocio sobre un `Order` existente.

Secuencia conceptual:

```text
1. Recibir orderId y OrderEvent
2. Cargar Order
3. Resolver la intencion con OrderStateMachine usando estado actual + evento
4. Si la resolucion es ALREADY_SATISFIED, devolver exito sin tocar stock ni crear historial
5. Si la resolucion es INVALID, devolver error de dominio
6. Si la resolucion es APPLICABLE(rule), ejecutar order.ensureCanApply(rule)
7. Si rule.requiredStockAction != NONE, obtener itemIds desde las lineas del pedido
8. Cargar Items referenciados por las lineas
9. Ejecutar la accion de stock requerida para cada linea
10. Ejecutar order.applyTransition(rule, occurredAt)
11. Persistir Order e Items dentro de la misma transaccion
```

Notas:

- La validacion de `Order` ocurre antes de tocar stock.
- `applyTransition(rule, occurredAt)` vuelve a proteger invariantes criticas del agregado.
- Los reintentos idempotentes no repiten acciones de stock ni duplican historial.
- Las operaciones de stock se ejecutan en `Item`, no en `Order`.

---

## Limite Transaccional

En Fase 1, cada caso de uso que afecte a `Order` y `Item` se ejecuta dentro de una unica transaccion local de base de datos.

Esto aplica a:

- creacion de pedido y reserva inicial de stock
- cancelacion y liberacion de stock
- envio y decremento de stock

No se introducen patrones de consistencia distribuida en esta fase.

---

## Trazabilidad Operacional

`traceId` pertenece a observabilidad operacional y no al agregado `Order`.

Toda request externa debe incluir `X-Trace-Id`. El adaptador REST valida su presencia antes de ejecutar casos de uso.

La capa de aplicacion puede propagar `traceId` hacia logs, trazas o middleware de observabilidad, pero no debe introducirlo en `OrderHistory` salvo que exista un requisito explicito de negocio o auditoria regulatoria.
