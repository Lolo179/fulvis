# Agent Task 006 - Implement CreateOrderUseCase

**Estado:** Accepted  
**Ultima revision:** 2026-08-26

---

## Task

```text
Nombre: Implement CreateOrderUseCase
Objetivo: Implementar el caso de uso de creacion de Order, coordinando reserva de stock en Item y creacion de historial inicial dentro del limite de aplicacion.
Tipo: aplicacion
Rama sugerida: feature/create-order-use-case
Worktree sugerido: worktrees/fulvis-create-order-use-case
```

---

## Nota de Orden

Esta tarea debe ejecutarse despues de `005-implement-item-domain.md` en estado `Accepted`.

Esta tarea implementa la orquestacion de aplicacion para crear pedidos, pero no implementa adaptadores REST, persistencia concreta ni locking pesimista real. La carga con lock queda expresada en el puerto `ItemRepositoryPort`.

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
specs/adr/ADR-003-modelado-transiciones-pedido.md
specs/architecture/architecture.spec.md
specs/use-cases/create-order.spec.md
specs/architecture/project-structure.spec.md
```

---

## Fuentes de Verdad Aplicables

```text
Requirements:
- REQ-S02
- REQ-P01
- RN-01
- RN-02
- RN-03
- NFR-CON-01
- NFR-AUD-01
- RT-01

Domain:
- Order
- OrderLine
- OrderHistory
- Item

Application:
- CreateOrderCommand
- CreateOrderLineCommand
- CreateOrderResult
- OrderRepositoryPort
- ItemRepositoryPort
- Clock
- CreateOrderUseCase

ADRs:
- ADR-001
- ADR-003

Specs:
- specs/architecture/architecture.spec.md
- specs/use-cases/create-order.spec.md
- specs/architecture/project-structure.spec.md

Quality gates:
- Casos de uso
- Responsabilidad de Order
- Responsabilidad de Item
- Limites de dominio
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
- Implementar locking pesimista real
- Implementar OrderStateMachine
- Implementar transiciones de Order existentes
```

---

## Restricciones

```text
- Respetar vocabulario tecnico en ingles definido en CONVENTIONS.md
- No modificar ADRs, specs ni quality gates
- No introducir decisiones de negocio en adaptadores
- No depender de controladores REST
- No depender de repositorios concretos
- No depender de entidades JPA
- CreateOrderUseCase debe depender de puertos
- CreateOrderUseCase no debe usar OrderStateMachine
- CreateOrderUseCase debe coordinar Item.reserve(quantity) antes de crear/persistir Order
- CreateOrderUseCase debe crear Order en estado MANAGED
- CreateOrderUseCase debe garantizar historial inicial null -> MANAGED
- traceId no debe entrar en Order, Item ni OrderHistory
- Marcar tensiones entre documentos antes de implementar
```

---

## Specs que Debe Satisfacer

```text
- specs/use-cases/create-order.spec.md
- specs/architecture/architecture.spec.md
- specs/architecture/project-structure.spec.md
```

---

## Quality Gates Aplicables

```text
- Rechaza lineas vacias o duplicadas por itemId
- Carga Items con puerto de repositorio
- Reserva stock antes de crear/persistir Order
- Crea Order en MANAGED
- Crea historial inicial null -> MANAGED
- Persiste Order e Items en una unica transaccion conceptual
- No usa OrderStateMachine para creacion
- No introduce dependencias de infraestructura en aplicacion
```

---

## Criterio de Done

La tarea se considera terminada cuando:

```text
- Existe CreateOrderCommand
- Existe CreateOrderLineCommand
- Existe CreateOrderResult
- Existe CreateOrderUseCase
- Existen OrderRepositoryPort e ItemRepositoryPort si no existian
- CreateOrderUseCase carga Items por itemId
- CreateOrderUseCase llama item.reserve(quantity) por cada linea
- CreateOrderUseCase crea Order en MANAGED
- CreateOrderUseCase devuelve CreateOrderResult
- CreateOrderUseCase no usa OrderStateMachine
- No hay dependencias de REST, JPA o repositorios concretos en aplicacion
- Existen unit tests de aplicacion en formato Given-When-Then
- Los tests cubren stock suficiente, stock insuficiente, lineas duplicadas, cantidad invalida e item inexistente
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
- specs/use-cases/create-order.spec.md
- specs/architecture/architecture.spec.md
- specs/APPLICATION.md
- specs/QUALITY_GATES.md
- ausencia de cambios en specs/**
```

La revision humana debe centrarse en orquestacion, limites de responsabilidad y cobertura de specs, no en revisar linea por linea toda la implementacion.
