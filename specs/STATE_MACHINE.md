# STATE_MACHINE.md - Fulvis / Order

**Estado:** Fase 1  
**Ultima revision:** 2026-08-17  
**Entidad modelada:** Order

---

## Estados

| Estado | Descripcion |
|---|---|
| `MANAGED` | Pedido creado y stock reservado. Pendiente de preparacion. |
| `IN_PREPARATION` | El operador ha iniciado la preparacion fisica del pedido. |
| `SHIPPED` | El pedido ha salido del almacen. Stock decrementado permanentemente. |
| `RECEIVED` | El operador ha confirmado la recepcion por el destinatario. |
| `CLOSED` | El operador ha cerrado el pedido manualmente. Estado terminal. |
| `CANCELLED` | El pedido fue cancelado. Stock liberado. Estado terminal. |

**Estado inicial:** `MANAGED`  
**Estados terminales:** `CLOSED`, `CANCELLED`

---

## Eventos

| Evento | Disparado por |
|---|---|
| `START_PREPARATION` | Llamada explicita del operador a la API |
| `MARK_SHIPPED` | Llamada explicita del operador a la API |
| `CONFIRM_RECEPTION` | Llamada explicita del operador a la API |
| `CLOSE` | Llamada explicita del operador a la API |
| `CANCEL` | Llamada explicita del operador a la API |

`ORDER_CREATED` es un evento de creacion y auditoria inicial, pero no forma parte del `OrderStateMachine` operativo porque todavia no existe un agregado `Order` sobre el que transicionar.

---

## Tabla de transiciones

| Estado origen | Evento | Estado destino | Condicion de guarda | Accion |
|---|---|---|---|---|
| `MANAGED` | `START_PREPARATION` | `IN_PREPARATION` | - | - |
| `IN_PREPARATION` | `MARK_SHIPPED` | `SHIPPED` | - | `Item.decrement(quantity)` |
| `SHIPPED` | `CONFIRM_RECEPTION` | `RECEIVED` | - | - |
| `RECEIVED` | `CLOSE` | `CLOSED` | - | - |
| `MANAGED` | `CANCEL` | `CANCELLED` | - | `Item.release(quantity)` |
| `IN_PREPARATION` | `CANCEL` | `CANCELLED` | - | `Item.release(quantity)` |

Cualquier combinacion (estado origen, evento) no listada en esta tabla es una transicion invalida y debe producir una excepcion de dominio.

La creacion del pedido queda fuera de esta tabla operativa:

| Flujo | Estado resultante | Condicion de guarda | Accion |
|---|---|---|---|
| Creacion de pedido | `MANAGED` | `Item.stockAvailable >= quantity` | `Item.reserve(quantity)` |

---

## Diagrama

```
CREATED -> MANAGED --CANCEL--> CANCELLED
              |
              | START_PREPARATION
              v
        IN_PREPARATION --CANCEL--> CANCELLED
              |
              | MARK_SHIPPED [Item.decrement]
              v
           SHIPPED
              |
              | CONFIRM_RECEPTION
              v
           RECEIVED
              |
              | CLOSE
              v
            CLOSED
```

---

## Notas de consistencia

- La accion `Item.reserve()` en la creacion y la escritura del pedido ocurren en la misma transaccion (RT-01, ADR-001).
- La creacion genera una entrada inicial en `OrderHistory` con `previousStatus = null`, `newStatus = MANAGED` y `timestamp = createdAt`.
- La accion `Item.release()` en la cancelacion y el cambio de estado ocurren en la misma transaccion (RT-01).
- La accion `Item.decrement()` en el envio y el cambio de estado ocurren en la misma transaccion (RT-01).
- Cada transicion genera una entrada en `OrderHistory` dentro de la misma transaccion (NFR-AUD-01).
- Las transiciones son idempotentes: si el pedido ya se encuentra exactamente en el estado destino directo del evento solicitado, la operacion se considera satisfecha sin error (ADR-002, ADR-003).
