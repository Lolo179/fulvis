# DOMAIN.md - Fulvis

**Estado:** Fase 1  
**Ultima revision:** 2026-08-17

---

## Vision general del dominio

Fulvis modela el ciclo de vida de pedidos sobre inventario fisico. El dominio central gira en torno a dos conceptos independientes: el pedido (con su estado y sus lineas) y el item (con su inventario). Un servicio de aplicacion orquesta las operaciones entre ambos sin contener logica de negocio.

---

## Agregados

### Order _(Aggregate Root)_

Representa un pedido en curso desde su creacion hasta su cierre o cancelacion.

**Responsabilidades:**
- Mantener y hacer cumplir su propio ciclo de vida de estados
- Rechazar transiciones de estado invalidas
- Gestionar sus lineas de pedido como parte de su propia consistencia
- Registrar cada transicion de estado en su historial

**Atributos principales:**
- `id`: identificador unico del pedido
- `status`: estado actual (MANAGED, IN_PREPARATION, SHIPPED, RECEIVED, CLOSED, CANCELLED)
- `createdAt`: timestamp de creacion
- `updatedAt`: timestamp de la ultima modificacion de estado
- `lines`: coleccion de OrderLine (1..N)
- `history`: coleccion de OrderHistory (1..N)

**Invariantes que el agregado hace cumplir:**
- Un pedido solo puede estar en un estado a la vez
- Las transiciones solo avanzan; no existe retroceso
- Solo puede cancelarse desde MANAGED o IN_PREPARATION
- La cancelacion libera el stock asociado en la misma operacion
- Un pedido no puede contener dos lineas con el mismo `itemId`

---

### OrderLine _(Entidad dentro del agregado Order)_

Representa una linea del pedido: un item concreto con la cantidad solicitada.

**Responsabilidades:**
- Mantener la referencia al item por identificador (no referencia directa al objeto Item)
- Mantener la cantidad solicitada
- Representar una unica necesidad de cantidad para un `itemId` dentro del pedido

**Atributos principales:**
- `itemId`: referencia al Item por identificador
- `quantity`: numero de unidades solicitadas

**Nota:** OrderLine no tiene ciclo de vida propio. Su existencia depende completamente del Order al que pertenece. Se crea con el pedido y no se modifica. En Fase 1 no se permiten lineas duplicadas para el mismo `itemId`.

---

### OrderHistory _(Entidad dentro del agregado Order)_

Registro inmutable de cada transicion de estado del pedido.

**Responsabilidades:**
- Persistir el estado anterior, el estado nuevo y el timestamp de cada transicion
- Proveer trazabilidad completa del ciclo de vida del pedido

**Atributos principales:**
- `previousStatus`: estado desde el que se transito
- `newStatus`: estado al que se transito
- `timestamp`: momento exacto de la transicion

**Nota:** OrderHistory es inmutable. Nunca se actualiza, solo se crea. Cualquier lectura de auditoria del pedido pasa por esta coleccion. La creacion del pedido genera una entrada inicial con `previousStatus = null`, `newStatus = MANAGED` y `timestamp = createdAt`.

---

### Item _(Aggregate Root independiente)_

Representa un producto fisico del catalogo con su estado de inventario.

**Responsabilidades:**
- Mantener los datos de catalogo del producto
- Gestionar sus propios invariantes de inventario
- Ejecutar `reserve`, `release` y `decrement` sobre su propio stock

**Atributos principales:**
- `id`: identificador unico
- `reference`: codigo de referencia del producto
- `name`: nombre descriptivo
- `stockTotal`: unidades fisicas totales en almacen
- `stockReserved`: unidades comprometidas con pedidos activos

**Valor derivado (no persistido):**
- `stockAvailable = stockTotal - stockReserved`

**Invariantes que el agregado hace cumplir:**
- `stockTotal >= 0` en todo momento
- `stockReserved >= 0` en todo momento
- `stockTotal >= stockReserved` en todo momento
- Una reserva solo puede ejecutarse si `stockAvailable >= requestedQuantity`

**Operaciones de dominio:**
- `reserve(quantity)`: incrementa `stockReserved`. Lanza excepcion de dominio si stock insuficiente. Ver ADR-001.
- `release(quantity)`: decrementa `stockReserved`. Llamado al cancelar un pedido.
- `decrement(quantity)`: reduce `stockTotal` y `stockReserved`. Llamado al marcar un pedido como SHIPPED.

---

## Value Objects

### OrderStatus _(Value Object)_

Representa los estados validos del ciclo de vida de un pedido. Es inmutable y no tiene identidad propia.

**Valores:** `MANAGED`, `IN_PREPARATION`, `SHIPPED`, `RECEIVED`, `CLOSED`, `CANCELLED`

---

## Politica de transiciones del pedido

La politica de transiciones del pedido se modela como una pieza explicita del dominio, separada del agregado `Order`.

**Vocabulario cerrado:**
- `OrderStatus`: estados posibles del pedido
- `OrderEvent`: eventos de negocio que solicitan una transicion
- `StockAction`: accion de inventario requerida por la transicion (`NONE`, `RESERVE`, `RELEASE`, `DECREMENT`)

**Regla de transicion:**

`OrderTransitionRule` representa una regla inmutable con:
- `from`: estado origen
- `event`: evento de negocio
- `to`: estado destino
- `requiredStockAction`: accion requerida sobre stock

**OrderStateMachine:**
- Conoce la tabla operativa de reglas definida en `STATE_MACHINE.md`
- Resuelve la intencion `estado actual + evento`
- Distingue entre transicion aplicable, operacion ya satisfecha y transicion invalida
- No valida guardas internas del pedido
- No ejecuta acciones de stock
- No cambia el estado del pedido
- No gestiona la creacion inicial del pedido

Resolucion conceptual:

```text
APPLICABLE(rule)       -> existe una transicion nueva que debe ejecutarse
ALREADY_SATISFIED     -> el pedido ya esta en un estado que satisface ese evento
INVALID               -> el evento no es valido para el estado actual
```

`ALREADY_SATISFIED` solo aplica cuando el estado actual coincide exactamente con el estado destino directo del evento solicitado. Un estado posterior distinto se trata como comando invalido o atrasado, no como reintento idempotente.

**Firma conceptual de Order:**

```java
order.applyTransition(rule, occurredAt);
```

`Order` recibe una `OrderTransitionRule` ya resuelta y el timestamp de la transicion. El agregado comprueba que la regla parte de su estado actual, valida sus guardas propias, cambia el estado y crea una entrada en `OrderHistory`.

`Order` no expone un setter de estado ni acepta cambios ciegos como `setStatus(...)`.

---

## Capa de aplicacion - Servicios de aplicacion (Casos de uso)

Los servicios de aplicacion orquestan operaciones entre agregados. No contienen logica de negocio ni conocen detalles de infraestructura. Se comunican con los agregados a traves de sus puertos (interfaces de repositorio).

**Responsabilidades:**
- Cargar agregados desde sus puertos
- Invocar metodos de dominio en el orden correcto
- Declarar el limite transaccional
- Delegar la persistencia a los puertos

**Lo que nunca hacen:**
- Contener condiciones de negocio (`if estado == X`)
- Acceder directamente a base de datos o infraestructura
- Tomar decisiones que pertenezcan a los agregados

---

## Relaciones entre agregados

Order referencia a Item **unicamente por identificador** (`itemId` en OrderLine). No existe referencia directa entre objetos. El servicio de aplicacion es responsable de cargar ambos agregados y coordinar la operacion entre ellos.

```
Order (id, status, createdAt)
  +-- OrderLine (itemId -> reference, quantity)
  +-- OrderHistory (previousStatus, newStatus, timestamp)

Item (id, reference, name, stockTotal, stockReserved)
```
