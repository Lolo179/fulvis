# Spec - CreateOrderUseCase

**Estado:** Fase 1  
**Ultima revision:** 2026-08-18

---

## Proposito

Definir el comportamiento esperado de `CreateOrderUseCase` en formato Given-When-Then.

Esta spec no describe implementacion ni framework de testing. Describe comportamiento observable que una implementacion debe satisfacer.

---

## Escenario 1 - Crear pedido con stock suficiente

**Given**
- Existe un `Item` con `stockTotal = 10` y `stockReserved = 2`
- Se solicita crear un `Order` con una linea para ese `itemId` y `quantity = 3`

**When**
- Se ejecuta `CreateOrderUseCase`

**Then**
- El `Item` incrementa `stockReserved` a `5`
- Se crea un `Order` en estado `MANAGED`
- El `Order` contiene una `OrderLine` con el `itemId` y `quantity = 3`
- El `Order` contiene un `OrderHistory` inicial con `previousStatus = null`, `newStatus = MANAGED` y `timestamp = createdAt`
- `CreateOrderResult` devuelve `orderId`, `status = MANAGED` y `createdAt`
- La reserva de stock y la creacion del `Order` ocurren dentro de la misma transaccion

---

## Escenario 2 - Rechazar creacion con stock insuficiente

**Given**
- Existe un `Item` con `stockTotal = 4` y `stockReserved = 2`
- Se solicita crear un `Order` con una linea para ese `itemId` y `quantity = 3`

**When**
- Se ejecuta `CreateOrderUseCase`

**Then**
- La operacion falla con `InsufficientStock`
- No se crea ningun `Order`
- No se modifica `stockReserved`
- No se crea `OrderHistory`

---

## Escenario 3 - Rechazar lineas duplicadas

**Given**
- Se solicita crear un `Order` con dos lineas para el mismo `itemId`

**When**
- Se ejecuta `CreateOrderUseCase`

**Then**
- La operacion falla con `InvalidOrderLines`
- No se reserva stock
- No se crea ningun `Order`

---

## Escenario 4 - Rechazar cantidad invalida

**Given**
- Se solicita crear un `Order` con una linea cuyo `quantity <= 0`

**When**
- Se ejecuta `CreateOrderUseCase`

**Then**
- La operacion falla con `InvalidOrderLines`
- No se reserva stock
- No se crea ningun `Order`

---

## Escenario 5 - Rechazar item inexistente

**Given**
- Se solicita crear un `Order` con una linea cuyo `itemId` no existe

**When**
- Se ejecuta `CreateOrderUseCase`

**Then**
- La operacion falla con `ItemNotFound`
- No se crea ningun `Order`
- No se crea `OrderHistory`

---

## Quality Gates Relacionados

- `Order` no permite lineas duplicadas por `itemId`
- `Item` protege `stockAvailable`
- La creacion no usa `OrderStateMachine`
- La creacion genera historial inicial
- `Order` e `Item` se persisten dentro de una unica transaccion
