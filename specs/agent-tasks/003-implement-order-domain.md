# Agent Task 003 - Implement Order Domain

**Estado:** Accepted  
**Ultima revision:** 2026-08-20

---

## Task

```text
Nombre: Implement Order domain
Objetivo: Implementar el modelo de dominio de Order, OrderLine, OrderHistory y la aplicacion de transiciones internas sin dependencias de infraestructura.
Tipo: dominio
Rama sugerida: feature/order-domain
Worktree sugerido: worktrees/fulvis-order-domain
```

---

## Nota de Orden

Esta tarea debe ejecutarse despues de `002-create-project-scaffold.md` en estado `Accepted`.

Esta tarea prepara el agregado `Order` para recibir una `OrderTransitionRule` ya resuelta, pero no implementa la tabla operativa ni la resolucion de transiciones. Esa responsabilidad queda para `004-implement-order-state-machine.md`.

---

## Contexto Obligatorio

El agente debe leer antes de trabajar:

```text
specs/CONVENTIONS.md
specs/REQUIREMENTS.md
specs/DOMAIN.md
specs/STATE_MACHINE.md
specs/APPLICATION.md
specs/QUALITY_GATES.md
specs/SPEC_COVERAGE.md
specs/adr/ADR-002-concurrencia-transiciones-estado.md
specs/adr/ADR-003-modelado-transiciones-pedido.md
specs/architecture/architecture.spec.md
specs/use-cases/transition-order.spec.md
```

---

## Fuentes de Verdad Aplicables

```text
Requirements:
- RN-04
- RN-05
- NFR-AUD-01

Domain:
- Order
- OrderLine
- OrderHistory
- OrderStatus
- OrderTransitionRule

State machine:
- Estados de Order
- Transiciones operativas
- Idempotencia por estado destino directo

ADRs:
- ADR-002
- ADR-003

Application:
- order.ensureCanApply(rule)
- order.applyTransition(rule, occurredAt)

Specs:
- specs/architecture/architecture.spec.md
- specs/use-cases/transition-order.spec.md

Quality gates:
- Vocabulario tecnico
- Limites de dominio
- Transiciones de estado
- Responsabilidad de Order
```

---

## Alcance

### Puede modificar

```text
- src/main/.../domain/**
- src/test/.../domain/**
```

### No puede modificar

```text
- specs/**
- adaptadores REST
- adaptadores de persistencia
- configuracion de Spring
- contratos OpenAPI
- casos de uso de aplicacion
```

### Fuera de alcance

```text
- Implementar Item
- Implementar OrderStateMachine
- Implementar la tabla operativa de transiciones
- Implementar resolucion APPLICABLE / ALREADY_SATISFIED / INVALID
- Implementar repositorios
- Implementar controladores REST
- Implementar persistencia JPA
- Implementar locking optimista real
- Implementar concurrencia real
```

---

## Restricciones

```text
- Respetar vocabulario tecnico en ingles definido en CONVENTIONS.md
- No introducir estados, eventos, errores o agregados no documentados
- No modificar ADRs, specs ni quality gates
- No introducir dependencias de Spring en dominio
- No introducir dependencias de JPA en dominio
- No introducir dependencias HTTP en dominio
- No crear setStatus publico ni mutadores ciegos de estado
- No hacer que Order ejecute operaciones de stock
- No hacer que Order referencie Item directamente
- Marcar tensiones entre documentos antes de implementar
```

---

## Specs que Debe Satisfacer

```text
- specs/architecture/architecture.spec.md
- specs/use-cases/transition-order.spec.md
```

---

## Quality Gates Aplicables

```text
- Order no expone setStatus
- Order aplica transiciones mediante OrderTransitionRule
- Order valida que rule.from coincide con status actual
- Order crea OrderHistory al aplicar una transicion nueva
- Order no ejecuta reserve/release/decrement
- OrderLine referencia Item solo mediante itemId
- Order no permite lineas duplicadas por itemId
- Dominio no depende de infraestructura
```

---

## Criterio de Done

La tarea se considera terminada cuando:

```text
- Existen Order, OrderLine y OrderHistory en dominio
- Existen OrderStatus, OrderEvent, StockAction y OrderTransitionRule como vocabulario minimo necesario para que Order aplique una regla ya resuelta
- Order puede ejecutar ensureCanApply(rule) sin mutar estado
- Order puede ejecutar applyTransition(rule, occurredAt)
- applyTransition(rule, occurredAt) registra OrderHistory
- No existe OrderStateMachine en esta tarea
- No existe tabla operativa de transiciones en esta tarea
- No existe setStatus publico
- No hay dependencias de Spring, JPA o HTTP en dominio
- Existen unit tests de dominio en formato Given-When-Then
- Los tests cubren transicion valida, transicion con rule.from incorrecto, historial y lineas duplicadas
- El agente reporta gaps, riesgos o tensiones detectadas
```

---

## Output Esperado del Agente

El agente debe entregar:

```text
Resumen del cambio:

Docs leidos:

Specs satisfechas:

Quality gates relevantes:

Archivos modificados:

Tensiones detectadas:

Gaps restantes:

Validaciones ejecutadas:
```

---

## Politica de Revision

El supervisor evaluara la tarea mediante:

```text
- diff
- tests unitarios de dominio
- specs/architecture/architecture.spec.md
- specs/use-cases/transition-order.spec.md
- specs/QUALITY_GATES.md
- ausencia de cambios en specs/**
```

La revision humana debe centrarse en desviaciones de arquitectura, gaps de cobertura y decisiones no justificadas, no en revisar linea por linea toda la implementacion.
