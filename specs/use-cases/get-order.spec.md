# Spec - GetOrderUseCase

**Estado:** Fase 1  
**Ultima revision:** 2026-08-18

---

## Proposito

Definir el comportamiento esperado de `GetOrderUseCase` en formato Given-When-Then.

Esta spec cubre la consulta de un `Order`, incluyendo lineas e historial.

---

## Escenario 1 - Consultar pedido existente

**Given**
- Existe un `Order` con `orderId = order-123`
- El `Order` esta en estado `IN_PREPARATION`
- El `Order` contiene una `OrderLine` con `itemId = item-123` y `quantity = 2`
- El `Order` contiene historial inicial `previousStatus = null`, `newStatus = MANAGED`
- El `Order` contiene historial de transicion `previousStatus = MANAGED`, `newStatus = IN_PREPARATION`

**When**
- Se ejecuta `GetOrderUseCase` para `orderId = order-123`

**Then**
- La operacion devuelve `orderId = order-123`
- La operacion devuelve `status = IN_PREPARATION`
- La operacion devuelve `createdAt`
- La operacion devuelve `updatedAt`
- La operacion devuelve `lines`
- La operacion devuelve `history`
- No se modifica `Order`
- No se modifica stock

---

## Escenario 2 - Consultar pedido inexistente

**Given**
- No existe un `Order` para el `orderId` solicitado

**When**
- Se ejecuta `GetOrderUseCase`

**Then**
- La operacion falla con `OrderNotFound`
- No se modifica ningun estado

---

## Quality Gates Relacionados

- La consulta no modifica `Order`
- La consulta no modifica `Item`
- La respuesta incluye `lines`
- La respuesta incluye `history`
- La API expone `GET /orders/{orderId}`
