# Agent Task 007 - Implement TransitionOrderUseCase

**Estado:** Draft  
**Ultima revision:** 2026-08-18

---

## Task

```text
Nombre: Implement TransitionOrderUseCase
Objetivo: Implementar el caso de uso de transicion de Order, resolviendo la politica con OrderStateMachine, coordinando acciones de stock en Item y preservando idempotencia.
Tipo: aplicacion
Rama sugerida: feature/transition-order-use-case
Worktree sugerido: worktrees/fulvis-transition-order-use-case
```

---

## Contexto Obligatorio

El agente debe leer antes de trabajar:

```text
specs/CONVENTIONS.md
specs/REQUIREMENTS.md
specs/DOMAIN.md
specs/APPLICATION.md
specs/STATE_MACHINE.md
specs/QUALITY_GATES.md
specs/SPEC_COVERAGE.md
specs/adr/ADR-001-concurrencia-reserva-stock.md
specs/adr/ADR-002-concurrencia-transiciones-estado.md
specs/adr/ADR-003-modelado-transiciones-pedido.md
specs/architecture/architecture.spec.md
specs/use-cases/transition-order.spec.md
specs/architecture/persistence-concurrency.spec.md
specs/architecture/project-structure.spec.md
```

---

## Fuentes de Verdad Aplicables

```text
Requirements:
- REQ-S03
- REQ-S04
- REQ-P02
- REQ-P03
- REQ-P04
- REQ-P05
- REQ-P06
- RN-04
- RN-05
- NFR-IDP-01
- NFR-CON-01
- NFR-AUD-01
- RT-01

Domain:
- Order
- OrderLine
- OrderHistory
- Item
- OrderStateMachine
- OrderTransitionRule
- StockAction

Application:
- TransitionOrderCommand
- TransitionOrderResult
- OrderRepositoryPort
- ItemRepositoryPort
- Clock
- TransitionOrderUseCase

ADRs:
- ADR-001
- ADR-002
- ADR-003

Specs:
- specs/architecture/architecture.spec.md
- specs/use-cases/transition-order.spec.md
- specs/architecture/persistence-concurrency.spec.md
- specs/architecture/project-structure.spec.md

Quality gates:
- Casos de uso
- Idempotencia y concurrencia
- Responsabilidad de Order
- Responsabilidad de Item
- Transiciones de estado
```

---

## Alcance

### Puede modificar

```text
- src/main/.../application/**
- src/test/.../application/**
- src/main/.../domain/** solo si necesita usar tipos ya definidos sin cambiar decisiones
- src/test/.../domain/** solo si necesita complementar tests relacionados
```

### No puede modificar

```text
- specs/**
- adaptadores REST
- adaptadores de persistencia
- contratos OpenAPI
- configuracion de Spring
```

### Fuera de alcance

```text
- Implementar controladores REST
- Implementar repositorios concretos
- Implementar JPA
- Implementar locking optimista real
- Implementar locking pesimista real
- Crear StockActionExecutor
- Cambiar la tabla de transiciones
- Cambiar reglas de idempotencia
```

---

## Restricciones

```text
- Respetar vocabulario tecnico en ingles definido en CONVENTIONS.md
- No modificar ADRs, specs ni quality gates
- No depender de controladores REST
- No depender de repositorios concretos
- No depender de entidades JPA
- TransitionOrderUseCase debe depender de puertos
- TransitionOrderUseCase debe usar OrderStateMachine
- TransitionOrderUseCase no debe decidir transiciones con if/switch propio
- TransitionOrderUseCase debe tratar ALREADY_SATISFIED sin tocar stock ni crear historial
- TransitionOrderUseCase debe tratar INVALID como error de dominio
- TransitionOrderUseCase debe ejecutar order.ensureCanApply(rule) antes de tocar stock
- TransitionOrderUseCase debe interpretar StockAction directamente por ahora
- TransitionOrderUseCase no debe crear StockActionExecutor
- TransitionOrderUseCase debe ejecutar order.applyTransition(rule, occurredAt) despues de la accion de stock
- traceId no debe entrar en Order, Item ni OrderHistory
- Marcar tensiones entre documentos antes de implementar
```

---

## Secuencia Obligatoria

```text
1. Cargar Order
2. Resolver la intencion con OrderStateMachine usando status actual + event
3. Si la resolucion es ALREADY_SATISFIED, devolver exito idempotente sin tocar stock ni crear historial
4. Si la resolucion es INVALID, devolver error de dominio
5. Si la resolucion es APPLICABLE(rule), ejecutar order.ensureCanApply(rule)
6. Si rule.requiredStockAction != NONE, obtener itemIds desde Order.lines
7. Cargar Items referenciados por las lineas
8. Ejecutar la accion de stock requerida para cada OrderLine
9. Ejecutar order.applyTransition(rule, occurredAt)
10. Persistir Order e Items dentro de la misma transaccion conceptual
```

---

## Specs que Debe Satisfacer

```text
- specs/use-cases/transition-order.spec.md
- specs/architecture/architecture.spec.md
- specs/architecture/persistence-concurrency.spec.md
- specs/architecture/project-structure.spec.md
```

---

## Quality Gates Aplicables

```text
- Usa OrderStateMachine para resolver transiciones
- No duplica la tabla de transiciones en aplicacion
- ALREADY_SATISFIED no toca stock
- ALREADY_SATISFIED no crea historial
- INVALID no toca stock
- order.ensureCanApply(rule) ocurre antes de tocar stock
- StockAction se interpreta en aplicacion sin crear StockActionExecutor
- order.applyTransition(rule, occurredAt) ocurre despues de la accion de stock
- Operaciones de stock se ejecutan en Item
- Order no ejecuta operaciones de stock
- Aplicacion depende de puertos
```

---

## Criterio de Done

La tarea se considera terminada cuando:

```text
- Existe TransitionOrderCommand
- Existe TransitionOrderResult
- Existe TransitionOrderUseCase
- TransitionOrderUseCase carga Order por OrderRepositoryPort
- TransitionOrderUseCase usa OrderStateMachine.resolve(...)
- TransitionOrderUseCase devuelve idempotent = true para ALREADY_SATISFIED
- TransitionOrderUseCase no toca stock ni crea historial en ALREADY_SATISFIED
- TransitionOrderUseCase falla con InvalidOrderTransition para INVALID
- TransitionOrderUseCase ejecuta order.ensureCanApply(rule) antes de stock
- TransitionOrderUseCase interpreta StockAction para NONE, RELEASE y DECREMENT
- TransitionOrderUseCase no introduce StockActionExecutor
- TransitionOrderUseCase ejecuta order.applyTransition(rule, occurredAt)
- No hay dependencias de REST, JPA o repositorios concretos en aplicacion
- Existen unit tests de aplicacion en formato Given-When-Then
- Los tests cubren transiciones validas, invalidas, idempotentes directas y comando atrasado
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
- tests unitarios de aplicacion
- specs/use-cases/transition-order.spec.md
- specs/architecture/architecture.spec.md
- specs/APPLICATION.md
- specs/STATE_MACHINE.md
- specs/QUALITY_GATES.md
- ausencia de cambios en specs/**
```

La revision humana debe centrarse en idempotencia, orden de orquestacion, efectos de stock y respeto de limites, no en revisar linea por linea toda la implementacion.
