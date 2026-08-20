# Spec - GetItemStockUseCase

**Estado:** Fase 1  
**Ultima revision:** 2026-08-18

---

## Proposito

Definir el comportamiento esperado de `GetItemStockUseCase` en formato Given-When-Then.

Esta spec cubre la consulta de stock sin modificar estado.

---

## Escenario 1 - Consultar stock existente

**Given**
- Existe un `Item` con `stockTotal = 10` y `stockReserved = 3`

**When**
- Se ejecuta `GetItemStockUseCase` para ese `itemId`

**Then**
- La operacion devuelve `itemId`
- La operacion devuelve `stockTotal = 10`
- La operacion devuelve `stockReserved = 3`
- La operacion devuelve `stockAvailable = 7`
- No se modifica `Item`

---

## Escenario 2 - Consultar item inexistente

**Given**
- No existe un `Item` para el `itemId` solicitado

**When**
- Se ejecuta `GetItemStockUseCase`

**Then**
- La operacion falla con `ItemNotFound`
- No se modifica ningun estado

---

## Quality Gates Relacionados

- `stockAvailable` es un valor derivado
- La consulta no modifica `Item`
- La API expone `GET /items/{itemId}/stock`
