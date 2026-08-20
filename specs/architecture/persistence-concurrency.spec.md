# Spec - Persistence and Concurrency

**Estado:** Pendiente de verificacion ejecutable  
**Ultima revision:** 2026-08-18

---

## Proposito

Definir comportamientos de persistencia, transaccion y concurrencia que deberan verificarse cuando exista implementacion con base de datos.

Esta spec no es ejecutable todavia. Debe evolucionar hacia integration tests con PostgreSQL real y Testcontainers.

---

## Escenario 1 - Reserva concurrente no sobrevende stock

**Given**
- Existe un `Item` con `stockTotal = 5` y `stockReserved = 0`
- Dos requests concurrentes intentan crear pedidos para el mismo `itemId`
- Cada request solicita `quantity = 4`

**When**
- Ambas operaciones ejecutan `CreateOrderUseCase` concurrentemente

**Then**
- Solo una operacion puede reservar stock correctamente
- La otra operacion falla con `InsufficientStock` o espera hasta observar stock insuficiente
- `stockReserved` no supera `stockTotal`
- No se crean pedidos inconsistentes
- La verificacion debe usar PostgreSQL real y locking pesimista segun `ADR-001`

---

## Escenario 2 - Creacion de pedido y reserva son atomicas

**Given**
- Existe un `Item` con stock suficiente
- Se ejecuta `CreateOrderUseCase`
- La persistencia del `Order` o de `OrderHistory` falla

**When**
- La transaccion termina

**Then**
- No se persiste el `Order`
- No se persiste `OrderHistory`
- No queda incrementado `stockReserved`
- La operacion completa hace rollback

---

## Escenario 3 - Cancelacion y liberacion son atomicas

**Given**
- Existe un `Order` en estado `MANAGED`
- Existe un `Item` con stock reservado para ese `Order`
- Se ejecuta `TransitionOrderUseCase` con `event = CANCEL`
- La persistencia del cambio de estado o del historial falla

**When**
- La transaccion termina

**Then**
- El `Order` no queda en `CANCELLED`
- No se persiste nuevo `OrderHistory`
- No queda liberado parcialmente el stock
- La operacion completa hace rollback

---

## Escenario 4 - Envio y decremento son atomicos

**Given**
- Existe un `Order` en estado `IN_PREPARATION`
- Existe un `Item` con stock reservado para ese `Order`
- Se ejecuta `TransitionOrderUseCase` con `event = MARK_SHIPPED`
- La persistencia del cambio de estado o del historial falla

**When**
- La transaccion termina

**Then**
- El `Order` no queda en `SHIPPED`
- No se persiste nuevo `OrderHistory`
- No queda decrementado parcialmente `stockTotal`
- No queda decrementado parcialmente `stockReserved`
- La operacion completa hace rollback

---

## Escenario 5 - Transicion concurrente sobre el mismo Order

**Given**
- Existe un `Order` en estado `MANAGED`
- Dos requests concurrentes intentan aplicar `event = START_PREPARATION`

**When**
- Ambas operaciones ejecutan `TransitionOrderUseCase` concurrentemente

**Then**
- Una operacion aplica la transicion a `IN_PREPARATION`
- La otra detecta conflicto de locking optimista o relee el estado actualizado
- La operacion repetida se considera `ALREADY_SATISFIED`
- No se crea historial duplicado
- No se produce estado inconsistente
- La verificacion debe usar locking optimista segun `ADR-002`

---

## Escenario 6 - Conflicto concurrente no satisfecho

**Given**
- Existe un `Order` en estado `MANAGED`
- Una request intenta aplicar `event = START_PREPARATION`
- Otra request concurrente intenta aplicar `event = CANCEL`

**When**
- Ambas operaciones compiten sobre el mismo `Order`

**Then**
- Solo una transicion se confirma
- La operacion perdedora relee el estado actual
- Si el estado actual no satisface su evento, la operacion falla con `ConcurrencyConflict` o `InvalidOrderTransition` segun la traduccion definida por la aplicacion
- No se duplican acciones de stock
- No se duplica historial

---

## Escenario 7 - Reintento post-conflicto satisfecho no repite stock

**Given**
- Existe un `Order` que ya alcanzo el estado destino directo de un evento con accion de stock
- El cliente reintenta el mismo evento por timeout o conflicto detectado

**When**
- Se ejecuta `TransitionOrderUseCase`

**Then**
- La operacion devuelve exito idempotente
- No se repite la accion de stock
- No se crea nuevo `OrderHistory`
- El estado persistido permanece consistente

---

## Quality Gates Relacionados

- Reserva de stock usa locking pesimista
- Transiciones de `Order` usan locking optimista
- Operaciones que afectan `Order` e `Item` usan una unica transaccion local
- Reintentos idempotentes no duplican efectos
- Fallos de persistencia hacen rollback completo
