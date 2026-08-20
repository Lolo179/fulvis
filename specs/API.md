# API.md - Fulvis

**Estado:** Fase 1  
**Ultima revision:** 2026-08-18

---

## Proposito

Este documento define el contrato REST conceptual de Fulvis para Fase 1.

La API actua como adaptador externo hacia los casos de uso definidos en `APPLICATION.md`. No contiene reglas de negocio ni decide transiciones de estado.

---

## Principios

- La API expresa intenciones de negocio, no mutaciones internas.
- El cliente no envia estados destino.
- Toda request externa debe incluir `X-Trace-Id`.
- Las transiciones se solicitan mediante eventos de negocio.
- Los adaptadores REST traducen requests a comandos de aplicacion.
- Los adaptadores REST traducen resultados y errores a respuestas HTTP.
- La politica de transicion permanece en `OrderStateMachine`.

---

## Endpoints minimos

Header obligatorio:

```http
X-Trace-Id: <trace-id>
```

```http
POST /orders
POST /orders/{orderId}/transitions
GET /orders/{orderId}
GET /items/{itemId}/stock
```

Si `X-Trace-Id` no esta presente, el adaptador REST debe rechazar la request como error de contrato.

---

## Crear pedido

```http
POST /orders
```

Request conceptual:

```json
{
  "lines": [
    {
      "itemId": "item-123",
      "quantity": 2
    }
  ]
}
```

Mapeo:

```text
REST request -> CreateOrderCommand
CreateOrderResult -> REST response
```

Respuesta exitosa conceptual:

```json
{
  "orderId": "order-123",
  "status": "MANAGED",
  "createdAt": "2026-08-18T10:15:30Z"
}
```

---

## Transicionar pedido

```http
POST /orders/{orderId}/transitions
```

Request conceptual:

```json
{
  "event": "MARK_SHIPPED"
}
```

El cliente solicita un evento de negocio. No solicita un estado destino.

Mapeo:

```text
REST request -> TransitionOrderCommand
TransitionOrderResult -> REST response
```

Respuesta exitosa conceptual:

```json
{
  "orderId": "order-123",
  "status": "SHIPPED",
  "occurredAt": "2026-08-18T10:20:00Z"
}
```

`TransitionOrderResult.idempotent` pertenece al resultado de aplicacion. No se expone necesariamente en la respuesta REST publica de Fase 1.

---

## Consultar pedido

```http
GET /orders/{orderId}
```

Respuesta conceptual:

```json
{
  "orderId": "order-123",
  "status": "IN_PREPARATION",
  "createdAt": "2026-08-18T10:15:30Z",
  "updatedAt": "2026-08-18T10:17:00Z",
  "lines": [
    {
      "itemId": "item-123",
      "quantity": 2
    }
  ],
  "history": [
    {
      "previousStatus": null,
      "newStatus": "MANAGED",
      "timestamp": "2026-08-18T10:15:30Z"
    },
    {
      "previousStatus": "MANAGED",
      "newStatus": "IN_PREPARATION",
      "timestamp": "2026-08-18T10:17:00Z"
    }
  ]
}
```

---

## Consultar stock

```http
GET /items/{itemId}/stock
```

Respuesta conceptual:

```json
{
  "itemId": "item-123",
  "stockTotal": 10,
  "stockReserved": 2,
  "stockAvailable": 8
}
```

`stockAvailable` es un valor derivado: `stockTotal - stockReserved`.

---

## Mapeo de errores

| Error conceptual | HTTP sugerido | Comentario |
|---|---|---|
| `OrderNotFound` | `404 Not Found` | El pedido solicitado no existe |
| `ItemNotFound` | `404 Not Found` | Uno o mas items solicitados no existen |
| `InvalidOrderTransition` | `409 Conflict` | El evento no es valido para el estado actual |
| `InsufficientStock` | `409 Conflict` | La operacion no puede completarse por stock insuficiente |
| `InvalidOrderLines` | `400 Bad Request` | La solicitud contiene lineas invalidas |
| `ConcurrencyConflict` | `409 Conflict` | Conflicto concurrente no resuelto como idempotente |
| Falta `X-Trace-Id` | `400 Bad Request` | Request externa sin trazabilidad operacional obligatoria |

---

## Decision sobre transiciones

Se usa:

```http
POST /orders/{orderId}/transitions
```

en lugar de:

```http
PATCH /orders/{orderId}
```

Motivo:

Una transicion de pedido no es una actualizacion parcial de un campo. Es una operacion de negocio con reglas, efectos sobre stock, historial e idempotencia. `POST /orders/{orderId}/transitions` expresa mejor que el cliente solicita procesar una intencion de negocio y que el sistema decide la transicion valida.
