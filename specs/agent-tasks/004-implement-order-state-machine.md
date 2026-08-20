# Agent Task 004 - Implement Order State Machine

**Estado:** Accepted  
**Ultima revision:** 2026-08-20

---

## Task

```text
Nombre: Implement OrderStateMachine
Objetivo: Implementar la politica de transiciones de Order y la resolucion APPLICABLE / ALREADY_SATISFIED / INVALID sin mezclar guardas, stock ni persistencia.
Tipo: dominio
Rama sugerida: feature/order-state-machine
Worktree sugerido: worktrees/fulvis-order-state-machine
```

---

## Nota de Orden

Esta tarea debe ejecutarse despues de `003-implement-order-domain.md` en estado `Accepted`.

Esta tarea implementa la politica de transiciones y su resolucion, pero no modifica las guardas internas de `Order` ni ejecuta acciones de stock. La aplicacion de una regla ya resuelta sigue siendo responsabilidad de `Order`.

---

## Contexto Obligatorio

El agente debe leer antes de trabajar:

```text
specs/CONVENTIONS.md
specs/DOMAIN.md
specs/STATE_MACHINE.md
specs/QUALITY_GATES.md
specs/SPEC_COVERAGE.md
specs/adr/ADR-003-modelado-transiciones-pedido.md
specs/architecture/architecture.spec.md
specs/use-cases/transition-order.spec.md
```

---

## Fuentes de Verdad Aplicables

```text
Domain:
- OrderStatus
- OrderEvent
- StockAction
- OrderTransitionRule
- OrderStateMachine

State machine:
- Tabla de transiciones operativas
- Creacion fuera del state machine operativo
- ALREADY_SATISFIED por estado destino directo

ADRs:
- ADR-003

Specs:
- specs/architecture/architecture.spec.md
- specs/use-cases/transition-order.spec.md

Quality gates:
- Transiciones de estado
- Responsabilidad de OrderStateMachine
- Vocabulario tecnico
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
- Implementar Order completo
- Implementar Item
- Ejecutar acciones de stock
- Validar guardas internas de Order
- Crear OrderHistory
- Gestionar creacion inicial de Order
- Implementar repositorios
- Implementar concurrencia real
```

---

## Restricciones

```text
- Respetar vocabulario tecnico en ingles definido en CONVENTIONS.md
- No introducir estados, eventos o acciones no documentados
- No modificar ADRs, specs ni quality gates
- No introducir dependencias de Spring en dominio
- No introducir dependencias de JPA en dominio
- No introducir dependencias HTTP en dominio
- No implementar guardas de Order dentro de OrderStateMachine
- No ejecutar reserve/release/decrement desde OrderStateMachine
- No incluir ORDER_CREATED como transicion operativa
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
- OrderStateMachine resuelve currentStatus + event
- OrderStateMachine devuelve APPLICABLE(rule), ALREADY_SATISFIED o INVALID
- ALREADY_SATISFIED solo aplica si status actual coincide con destino directo
- OrderStateMachine no valida guardas internas de Order
- OrderStateMachine no ejecuta acciones de stock
- OrderStateMachine no persiste entidades
- OrderStateMachine no gestiona ORDER_CREATED como transicion operativa
```

---

## Criterio de Done

La tarea se considera terminada cuando:

```text
- Existen OrderStatus, OrderEvent y StockAction
- Existe OrderTransitionRule inmutable
- Existe OrderStateMachine
- OrderStateMachine contiene la tabla operativa de transiciones definida en STATE_MACHINE.md
- OrderStateMachine resuelve APPLICABLE(rule) para transiciones validas
- OrderStateMachine resuelve ALREADY_SATISFIED para estado destino directo
- OrderStateMachine resuelve INVALID para transiciones no permitidas y comandos atrasados
- ORDER_CREATED no forma parte de las transiciones operativas
- Existen unit tests en formato Given-When-Then
- No hay dependencias de Spring, JPA o HTTP en dominio
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
- specs/STATE_MACHINE.md
- specs/adr/ADR-003-modelado-transiciones-pedido.md
- ausencia de cambios en specs/**
```

La revision humana debe centrarse en desviaciones de responsabilidad, cobertura de transiciones e idempotencia, no en revisar linea por linea toda la implementacion.
