# Spec - TransitionOrderUseCase

**Estado:** Fase 1  
**Ultima revision:** 2026-08-18

---

## Proposito

Definir el comportamiento esperado de `TransitionOrderUseCase` en formato Given-When-Then.

Esta spec no describe implementacion ni framework de testing. Describe comportamiento observable que una implementacion debe satisfacer.

---

## Escenario 1 - Iniciar preparacion

**Given**
- Existe un `Order` en estado `MANAGED`

**When**
- Se ejecuta `TransitionOrderUseCase` con `event = START_PREPARATION`

**Then**
- El `Order` pasa a estado `IN_PREPARATION`
- Se crea un `OrderHistory` con `previousStatus = MANAGED` y `newStatus = IN_PREPARATION`
- No se modifica stock
- `TransitionOrderResult` devuelve `status = IN_PREPARATION` e `idempotent = false`

---

## Escenario 2 - Marcar como enviado

**Given**
- Existe un `Order` en estado `IN_PREPARATION`
- El `Order` tiene una `OrderLine` con `itemId = item-123` y `quantity = 2`
- Existe el `Item item-123` con `stockTotal = 10` y `stockReserved = 2`

**When**
- Se ejecuta `TransitionOrderUseCase` con `event = MARK_SHIPPED`

**Then**
- El `Item` decrementa `stockTotal` a `8`
- El `Item` decrementa `stockReserved` a `0`
- El `Order` pasa a estado `SHIPPED`
- Se crea un `OrderHistory` con `previousStatus = IN_PREPARATION` y `newStatus = SHIPPED`
- `TransitionOrderResult` devuelve `status = SHIPPED` e `idempotent = false`

---

## Escenario 3 - Confirmar recepcion

**Given**
- Existe un `Order` en estado `SHIPPED`

**When**
- Se ejecuta `TransitionOrderUseCase` con `event = CONFIRM_RECEPTION`

**Then**
- El `Order` pasa a estado `RECEIVED`
- Se crea un `OrderHistory` con `previousStatus = SHIPPED` y `newStatus = RECEIVED`
- No se modifica stock
- `TransitionOrderResult` devuelve `status = RECEIVED` e `idempotent = false`

---

## Escenario 4 - Cerrar pedido

**Given**
- Existe un `Order` en estado `RECEIVED`

**When**
- Se ejecuta `TransitionOrderUseCase` con `event = CLOSE`

**Then**
- El `Order` pasa a estado `CLOSED`
- Se crea un `OrderHistory` con `previousStatus = RECEIVED` y `newStatus = CLOSED`
- No se modifica stock
- `TransitionOrderResult` devuelve `status = CLOSED` e `idempotent = false`

---

## Escenario 5 - Cancelar pedido gestionado

**Given**
- Existe un `Order` en estado `MANAGED`
- El `Order` tiene una `OrderLine` con `itemId = item-123` y `quantity = 2`
- Existe el `Item item-123` con `stockTotal = 10` y `stockReserved = 2`

**When**
- Se ejecuta `TransitionOrderUseCase` con `event = CANCEL`

**Then**
- El `Item` decrementa `stockReserved` a `0`
- El `Order` pasa a estado `CANCELLED`
- Se crea un `OrderHistory` con `previousStatus = MANAGED` y `newStatus = CANCELLED`
- `TransitionOrderResult` devuelve `status = CANCELLED` e `idempotent = false`

---

## Escenario 6 - Cancelar pedido en preparacion

**Given**
- Existe un `Order` en estado `IN_PREPARATION`
- El `Order` tiene una `OrderLine` con `itemId = item-123` y `quantity = 2`
- Existe el `Item item-123` con `stockTotal = 10` y `stockReserved = 2`

**When**
- Se ejecuta `TransitionOrderUseCase` con `event = CANCEL`

**Then**
- El `Item` decrementa `stockReserved` a `0`
- El `Order` pasa a estado `CANCELLED`
- Se crea un `OrderHistory` con `previousStatus = IN_PREPARATION` y `newStatus = CANCELLED`
- `TransitionOrderResult` devuelve `status = CANCELLED` e `idempotent = false`

---

## Escenario 7 - Reintento idempotente

**Given**
- Existe un `Order` en estado `CANCELLED`

**When**
- Se ejecuta `TransitionOrderUseCase` con `event = CANCEL`

**Then**
- La operacion devuelve exito
- No se modifica stock
- No se crea un nuevo `OrderHistory`
- `TransitionOrderResult` devuelve `status = CANCELLED` e `idempotent = true`

---

## Escenario 8 - Reintento idempotente de iniciar preparacion

**Given**
- Existe un `Order` en estado `IN_PREPARATION`

**When**
- Se ejecuta `TransitionOrderUseCase` con `event = START_PREPARATION`

**Then**
- La operacion devuelve exito
- No se modifica stock
- No se crea un nuevo `OrderHistory`
- `TransitionOrderResult` devuelve `status = IN_PREPARATION` e `idempotent = true`

---

## Escenario 9 - Reintento idempotente de marcar como enviado

**Given**
- Existe un `Order` en estado `SHIPPED`

**When**
- Se ejecuta `TransitionOrderUseCase` con `event = MARK_SHIPPED`

**Then**
- La operacion devuelve exito
- No se modifica stock
- No se crea un nuevo `OrderHistory`
- `TransitionOrderResult` devuelve `status = SHIPPED` e `idempotent = true`

---

## Escenario 10 - Reintento idempotente de confirmar recepcion

**Given**
- Existe un `Order` en estado `RECEIVED`

**When**
- Se ejecuta `TransitionOrderUseCase` con `event = CONFIRM_RECEPTION`

**Then**
- La operacion devuelve exito
- No se modifica stock
- No se crea un nuevo `OrderHistory`
- `TransitionOrderResult` devuelve `status = RECEIVED` e `idempotent = true`

---

## Escenario 11 - Reintento idempotente de cerrar pedido

**Given**
- Existe un `Order` en estado `CLOSED`

**When**
- Se ejecuta `TransitionOrderUseCase` con `event = CLOSE`

**Then**
- La operacion devuelve exito
- No se modifica stock
- No se crea un nuevo `OrderHistory`
- `TransitionOrderResult` devuelve `status = CLOSED` e `idempotent = true`

---

## Escenario 12 - Comando atrasado no idempotente

**Given**
- Existe un `Order` en estado `SHIPPED`

**When**
- Se ejecuta `TransitionOrderUseCase` con `event = START_PREPARATION`

**Then**
- La operacion falla con `InvalidOrderTransition`
- No se modifica stock
- No se crea un nuevo `OrderHistory`

---

## Escenario 13 - Transicion invalida

**Given**
- Existe un `Order` en estado `SHIPPED`

**When**
- Se ejecuta `TransitionOrderUseCase` con `event = CANCEL`

**Then**
- La operacion falla con `InvalidOrderTransition`
- No se modifica stock
- No se crea un nuevo `OrderHistory`

---

## Escenario 14 - Pedido inexistente

**Given**
- No existe un `Order` para el `orderId` solicitado

**When**
- Se ejecuta `TransitionOrderUseCase`

**Then**
- La operacion falla con `OrderNotFound`
- No se resuelve `OrderStateMachine`
- No se modifica stock

---

## Escenario 15 - Item inexistente en transicion con stock

**Given**
- Existe un `Order` en estado `IN_PREPARATION`
- El `Order` tiene una `OrderLine` con un `itemId` inexistente

**When**
- Se ejecuta `TransitionOrderUseCase` con `event = MARK_SHIPPED`

**Then**
- La operacion falla con `ItemNotFound`
- El `Order` no cambia de estado
- No se crea un nuevo `OrderHistory`

---

## Quality Gates Relacionados

- Las transiciones se solicitan mediante `OrderEvent`
- `OrderStateMachine` resuelve `APPLICABLE`, `ALREADY_SATISFIED` o `INVALID`
- `ALREADY_SATISFIED` solo aplica sobre el estado destino directo
- Una transicion invalida no toca stock
- Un reintento idempotente no toca stock ni duplica historial
- `Order` no ejecuta operaciones de stock
- `Item` protege sus invariantes de inventario
