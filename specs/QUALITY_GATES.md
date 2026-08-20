# QUALITY_GATES.md - Fulvis

**Estado:** Fase 1  
**Ultima revision:** 2026-08-18

---

## Proposito

Este documento define quality gates para futuras implementaciones de Fulvis.

No describe tests concretos de framework. Traduce requisitos, decisiones arquitectonicas y limites de diseno en comportamientos observables que una implementacion debe demostrar.

La idea central es reducir la necesidad de revisar codigo generado linea por linea. El codigo puede ser aceptable si respeta contratos, pasa pruebas y cumple restricciones arquitectonicas explicitas.

---

## Principio de Trabajo con IA

El supervisor no debe intentar compensar codigo generado con revision manual exhaustiva.

Debe invertir criterio antes de la generacion:

- requisitos claros
- limites de responsabilidad
- contratos de aplicacion
- API estable
- ADRs explicitas
- convenciones de vocabulario
- pruebas y checks verificables

La revision humana se concentra en decisiones, riesgos, tensiones entre documentos y cobertura insuficiente.

---

## Puertas de Calidad

Una implementacion de Fulvis debe superar estas puertas antes de considerarse aceptable.

### 1. Vocabulario tecnico

Debe usar el vocabulario definido en `CONVENTIONS.md`.

Checks esperados:

- No aparecen estados antiguos como `GESTIONADO`, `EN_PREPARACION`, `ENVIADO`, `RECIBIDO`, `CERRADO`, `CANCELADO`.
- No aparecen entidades tecnicas antiguas como `PEDIDO`, `LINEA_PEDIDO`, `PEDIDO_HISTORIAL` o `ITEM`.
- Estados, eventos, comandos, resultados y campos publicos usan nombres en ingles.

### 2. Limites de dominio

El dominio debe estar aislado de detalles externos.

Checks esperados:

- `Order` no conoce HTTP, controladores, DTOs REST, repositorios concretos ni anotaciones de infraestructura.
- `Item` no conoce `Order`.
- `OrderLine` referencia `Item` unicamente mediante `itemId`.
- Adaptadores externos no deciden reglas de negocio.

### 3. Transiciones de estado

Las transiciones deben respetar `STATE_MACHINE.md` y `ADR-003`.

Checks esperados:

- El cliente o adaptador no puede cambiar estado mediante `setStatus`.
- Las transiciones se solicitan mediante `OrderEvent`.
- `OrderStateMachine` resuelve `APPLICABLE`, `ALREADY_SATISFIED` o `INVALID`.
- `ALREADY_SATISFIED` solo aplica si el estado actual coincide exactamente con el estado destino directo.
- Una transicion invalida no modifica stock, estado ni historial.

### 4. Responsabilidad de `Order`

`Order` protege sus invariantes internas.

Checks esperados:

- `Order` rechaza aplicar una `OrderTransitionRule` cuyo `from` no coincide con su `status`.
- `Order` valida sus guardas propias antes de aplicar una transicion.
- `Order` crea `OrderHistory` al aplicar una transicion nueva.
- `Order` no ejecuta operaciones de stock.
- `Order` no permite lineas duplicadas por `itemId`.

### 5. Responsabilidad de `Item`

`Item` protege invariantes de inventario.

Checks esperados:

- `stockTotal >= 0`.
- `stockReserved >= 0`.
- `stockTotal >= stockReserved`.
- `reserve(quantity)` falla si `stockAvailable < quantity`.
- `release(quantity)` no permite liberar mas de lo reservado.
- `decrement(quantity)` reduce `stockTotal` y `stockReserved` de forma consistente.

### 6. Casos de uso

La capa de aplicacion orquesta sin absorber reglas internas.

Checks esperados para `CreateOrderUseCase`:

- Rechaza lineas vacias o duplicadas por `itemId`.
- Carga `Item` con la estrategia de concurrencia definida en `ADR-001`.
- Reserva stock antes de crear/persistir el `Order`.
- Crea `Order` en `MANAGED`.
- Crea historial inicial `null -> MANAGED`.
- Persiste `Order` e `Items` en la misma transaccion.

Checks esperados para `TransitionOrderUseCase`:

- Carga `Order` antes de resolver la intencion.
- Usa `OrderStateMachine`.
- Si la resolucion es `ALREADY_SATISFIED`, devuelve exito sin tocar stock ni crear historial.
- Si la resolucion es `INVALID`, devuelve error de dominio sin tocar stock.
- Ejecuta `order.ensureCanApply(rule)` antes de tocar stock.
- Ejecuta acciones de stock por cada `OrderLine` cuando aplica.
- Ejecuta `order.applyTransition(rule, occurredAt)` despues de la accion de stock.
- Persiste `Order` e `Items` en la misma transaccion.

### 7. Idempotencia y concurrencia

Debe respetar `ADR-001` y `ADR-002`.

Checks esperados:

- Reserva de stock usa locking pesimista.
- Transiciones de `Order` usan locking optimista.
- Un reintento ya satisfecho no repite operaciones de stock.
- Un reintento ya satisfecho no duplica `OrderHistory`.
- Un conflicto concurrente no satisfecho se traduce a `ConcurrencyConflict`.

### 8. API REST

La API debe adaptar comandos de aplicacion sin exponer mutaciones internas.

Checks esperados:

- Toda request externa debe incluir `X-Trace-Id`.
- Existe `POST /orders`.
- Existe `POST /orders/{orderId}/transitions`.
- No se usa `PATCH /orders/{orderId}` para cambiar `status`.
- El cliente envia `event`, no `status` destino.
- `TransitionOrderResult.idempotent` no se expone obligatoriamente en REST publico.
- Errores conceptuales se mapean segun `API.md`.
- `traceId` no se introduce en `Order`, `Item` ni `OrderHistory`.

### 9. Version control and agent workflow

El trabajo debe ser trazable y apto para supervision por agentes.

Checks esperados:

- El proyecto usa Git.
- El remoto principal vive en GitHub.
- `main` representa el estado aceptado.
- Cambios no triviales se realizan en ramas separadas.
- PRs indican requisitos, specs, ADRs o quality gates afectados.
- Worktrees pueden usarse para experimentos multiagente.
- Salidas de agentes distintos se comparan mediante diffs, specs y quality gates.

---

## Criterio de Aceptacion

Una implementacion generada por IA puede considerarse candidata a aceptacion cuando:

- respeta los documentos de arquitectura
- supera pruebas de dominio y aplicacion
- supera checks estructurales de limites entre capas
- no introduce vocabulario tecnico no documentado
- no contradice ADRs aceptadas

Pasar pruebas no significa que el diseno sea perfecto. Significa que la implementacion cumple el marco de validez que el equipo ha definido hasta ese momento.

---

## Uso Futuro en SDD y Agentes

Este documento puede evolucionar hacia specs ejecutables, checks automaticos o instrucciones para agentes.

Antes de pedir a un agente que implemente Fulvis, el supervisor deberia proporcionarle como contexto:

- `REQUIREMENTS.md`
- `DOMAIN.md`
- `STATE_MACHINE.md`
- `APPLICATION.md`
- `API.md`
- `CONVENTIONS.md`
- ADRs relevantes
- este documento de verificacion

Si un agente propone una implementacion que no puede satisfacer estas puertas, debe corregirse la implementacion o revisarse explicitamente la decision arquitectonica afectada.
