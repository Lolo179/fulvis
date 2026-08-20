# ADR-003 - Modelado de transiciones del pedido

**Estado:** Aceptado  
**Fecha:** 2026-08-18  
**Autor:** Luis M.

---

## Contexto

Fulvis ya define el ciclo de vida del agregado `Order` en [STATE_MACHINE.md](C:/repositorio/proyect/specs/STATE_MACHINE.md), incluyendo:

- estados posibles
- eventos de negocio
- transiciones validas
- acciones asociadas sobre stock

Ademas, existen decisiones previas sobre concurrencia:

- `ADR-001`: locking pesimista para reserva de stock
- `ADR-002`: locking optimista e idempotencia para transiciones de estado del pedido

Queda por decidir como representar en el modelo de dominio la politica de transiciones del pedido sin:

- dispersar reglas en `if/else` o `switch`
- inflar la entidad `Order`
- mezclar la logica de stock con la logica del pedido
- trasladar toda la inteligencia del dominio al servicio de aplicacion

---

## Problema

El agregado `Order` debe proteger sus invariantes y aplicar solo transiciones validas, pero la politica de transicion no debe quedar:

- codificada de forma dispersa en multiples ramas condicionales
- acoplada a infraestructura o a servicios de aplicacion
- mezclada con la logica propia del agregado `Item`

Tambien es necesario mantener una separacion clara entre:

- reglas de negocio: que transiciones existen y que efectos requieren
- decisiones de arquitectura: donde vive esa politica y quien la ejecuta

---

## Alternativas consideradas

| Alternativa | Motivo de descarte |
|---|---|
| `if/else` o `switch` en `Order` | Hace explicita la logica, pero la vuelve mas fragil, menos mantenible y mas propensa a dispersion segun crezcan eventos y estados |
| `Strategy` con una clase por transicion y un router | Introduce una estructura mas orientada a patron que al dominio; riesgo de sobreingenieria temprana y de trasladar el problema a un router central |
| `enum` gigante con toda la logica embebida | Puede empezar simple, pero tiende a concentrar demasiadas responsabilidades y a acoplar politica, guardas y efectos en una sola pieza |
| Servicio de aplicacion resolviendo y aplicando directamente el nuevo estado | Debilita el agregado `Order`, que pasaria a comportarse como contenedor mutable en lugar de proteger sus invariantes |

---

## Decision

La politica de transiciones del pedido se modelara con las siguientes piezas:

- `OrderStatus` como `enum` para el conjunto cerrado de estados del pedido
- `OrderEvent` como `enum` para el conjunto cerrado de eventos de negocio
- `StockAction` como `enum` para expresar el efecto requerido sobre stock
- `OrderTransitionRule` como `record` inmutable con la forma `from + event + to + requiredStockAction`
- `OrderStateMachine` como componente de dominio encargado de resolver la intencion `estado actual + evento`

La distribucion de responsabilidades sera:

- `OrderStateMachine`: conoce la politica de transicion y devuelve una resolucion explicita para `estado actual + evento`
- `Order`: valida sus guardas propias, aplica la transicion valida y registra historial
- `Item`: protege invariantes de stock y ejecuta operaciones como reserve, release o decrement
- `Application Service`: orquesta `Order` e `Item` dentro de una unica transaccion de negocio

`Order` no conocera la tabla completa de transiciones ni resolvera por si mismo la politica del state machine.

`OrderStateMachine` expondra una operacion conceptual:

```java
stateMachine.resolve(currentStatus, event);
```

La resolucion podra representar tres casos:

```text
APPLICABLE(rule)       -> existe una transicion nueva que debe ejecutarse
ALREADY_SATISFIED     -> el pedido ya esta en un estado que satisface ese evento
INVALID               -> el evento no es valido para el estado actual
```

Esta resolucion permite tratar reintentos idempotentes sin volver a ejecutar acciones de stock ni crear historial duplicado.

`ALREADY_SATISFIED` solo se aplica cuando el estado actual del pedido coincide exactamente con el estado destino directo del evento solicitado.

Ejemplos:

```text
START_PREPARATION -> satisfecho solo en IN_PREPARATION
MARK_SHIPPED      -> satisfecho solo en SHIPPED
CONFIRM_RECEPTION -> satisfecho solo en RECEIVED
CLOSE              -> satisfecho solo en CLOSED
CANCEL            -> satisfecho solo en CANCELLED
```

Si el pedido se encuentra en un estado posterior distinto del destino directo, el comando se considera invalido para el estado actual. Esta regla evita enmascarar comandos atrasados como reintentos idempotentes.

`Order` no aceptara cambios ciegos de estado como `setStatus(...)`; en su lugar aplicara una `OrderTransitionRule` ya resuelta.

La firma conceptual inicial sera:

```java
order.applyTransition(rule, occurredAt);
```

No se introducira inicialmente un objeto `OrderAuditData`, porque la auditoria minima exigida por `NFR-AUD-01` solo requiere timestamp junto al estado anterior y el estado nuevo. Datos como `requestId`, `traceId`, `ip` o `userAgent` pertenecen a trazabilidad operacional y quedan fuera del agregado.

---

## Guardas del pedido

Las guardas propias de `Order` se implementaran inicialmente como metodos internos simples del agregado.

Ejemplos de guardas:

- el pedido debe tener al menos una linea
- las lineas deben tener cantidades validas
- los datos requeridos para la transicion deben estar completos
- la regla recibida debe partir del estado actual del pedido

No se modelaran inicialmente como estrategias, predicados externos ni objetos independientes, porque en esta fase las guardas pertenecen claramente al agregado y no justifican una abstraccion adicional.

Esta decision no prohibe refactorizaciones futuras si las guardas crecen, se vuelven reutilizables o empiezan a necesitar composicion explicita.

---

## Creacion del pedido

La creacion de un pedido no forma parte del `OrderStateMachine` operativo.

Aunque `STATE_MACHINE.md` conserva `ORDER_CREATED` como evento de creacion y auditoria inicial, no se modelara como una transicion desde un pseudoestado `(ninguno)`.

Motivos:

- una transicion presupone que el agregado `Order` ya existe
- la creacion construye identidad, lineas, fechas y estado inicial
- incluir un estado inexistente en el state machine complica el modelo por simetria artificial
- la reserva inicial de stock pertenece al caso de uso de creacion y se ejecuta en la misma transaccion que la persistencia del pedido

La creacion queda bajo responsabilidad del caso de uso o factory de creacion:

```text
1. Validar solicitud de creacion
2. Reservar stock para cada linea
3. Crear Order en estado MANAGED
4. Crear historial inicial null -> MANAGED
5. Persistir Order e Items dentro de la misma transaccion
```

El historial inicial se registra con:

```text
previousStatus = null
newStatus = MANAGED
timestamp = createdAt
```

---

## Secuencia del caso de uso

El servicio de aplicacion debe validar las guardas propias del pedido antes de ejecutar cualquier accion sobre stock.

Secuencia conceptual:

```text
1. Cargar Order
2. Resolver la intencion con OrderStateMachine
3. Si la resolucion es ALREADY_SATISFIED, devolver exito sin tocar stock ni crear historial
4. Si la resolucion es INVALID, devolver error de dominio
5. Si la resolucion es APPLICABLE(rule), ejecutar order.ensureCanApply(rule)
6. Obtener los itemIds desde las lineas del pedido
7. Cargar los Items referenciados por las lineas
8. Ejecutar la accion de stock requerida para cada linea, si rule.requiredStockAction != NONE
9. Ejecutar order.applyTransition(rule, occurredAt)
10. Persistir Order e Items dentro de la misma transaccion
```

`ensureCanApply(rule)` valida las guardas del pedido sin mutar estado.

`applyTransition(rule, occurredAt)` aplica el cambio de estado y registra historial. Aunque el caso de uso haya llamado antes a `ensureCanApply(rule)`, este metodo debe volver a proteger las invariantes criticas del agregado, especialmente que `rule.from()` coincida con el estado actual.

Esta separacion permite:

- evitar tocar stock si el pedido no puede transicionar
- evitar cambiar el pedido si la accion de stock falla
- mantener el agregado protegido frente a usos incorrectos
- hacer explicito el orden de la transaccion de negocio

---

## Lineas de pedido y stock

Las acciones de stock se aplican por cada `OrderLine`, usando el `itemId` y la `quantity` de la linea.

En Fase 1, `Order` no permitira dos lineas con el mismo `itemId`.

Esta regla reduce complejidad de orquestacion y evita tener que agrupar cantidades antes de `reserve`, `release` o `decrement` stock. Si en el futuro negocio requiere lineas duplicadas para un mismo item, la politica debera revisarse y la aplicacion debera agregar cantidades por `itemId` antes de operar sobre `Item`.

---

## Interpretacion de StockAction

`StockAction` declara la accion de inventario requerida por una transicion, pero no la ejecuta.

El servicio de aplicacion interpretara inicialmente `StockAction` de forma explicita:

```text
NONE      -> no ejecutar accion de stock
RESERVE   -> item.reserve(quantity)
RELEASE   -> item.release(quantity)
DECREMENT -> item.decrement(quantity)
```

No se introducira inicialmente un `StockActionExecutor` como abstraccion separada. En esta fase hay pocas acciones, no existen multiples implementaciones y la interpretacion forma parte de la orquestacion del caso de uso.

Esta decision permite mantener visible el flujo de aplicacion sin trasladar reglas de negocio al servicio. El servicio coordina agregados; no decide si una transicion es valida ni valida invariantes internas de `Order` o `Item`.

Un `StockActionExecutor` podra considerarse en el futuro si aparecen mas acciones, reutilizacion entre casos de uso, reglas operativas complejas o necesidad de desacoplar la interpretacion de stock del servicio de aplicacion.

---

## Justificacion

- La politica de transiciones queda explicita, cerrada y centralizada.
- Se evita dispersar reglas de negocio en ramas condicionales.
- `Order` mantiene comportamiento real de dominio sin convertirse en un god object.
- La logica de stock permanece en `Item`, respetando el limite entre agregados.
- El servicio de aplicacion orquesta, pero no absorbe toda la inteligencia del dominio.
- `record` encaja semanticamente con una regla inmutable que representa politica y no identidad mutable.
- El modelo resultante es mas facil de razonar, documentar y consumir desde un harness o desde agentes que necesiten contexto estructurado.

---

## Consecuencias

- El modelo de dominio tendra mas piezas explicitas, aunque cada una con una responsabilidad mas acotada.
- Cambiar una transicion implicara modificar la politica centralizada en `OrderStateMachine`, en lugar de buscar logica repartida.
- Las guardas propias del pedido seguiran viviendo en `Order`, no en el state machine.
- Las operaciones de stock seguiran siendo responsabilidad de `Item`, incluso cuando una transicion del pedido requiera una accion de inventario.
- La implementacion debera reflejar de forma clara la secuencia: resolver regla, validar guardas, coordinar stock, aplicar transicion, registrar historial.

---

## Relacion con otras decisiones

- `STATE_MACHINE.md` sigue siendo la fuente de verdad para reglas de negocio, estados, eventos, guardas y acciones esperadas.
- `ADR-001` define el control de concurrencia para reserva de stock.
- `ADR-002` define el control de concurrencia e idempotencia para transiciones de estado del pedido.
- Esta ADR define la representacion y ubicacion de la politica de transiciones dentro del modelo de dominio.
