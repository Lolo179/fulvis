# Agent Task 009 - Implement GetOrderUseCase

**Estado:** Accepted  
**Ultima revision:** 2026-08-26

---

## Task

```text
Nombre: Implement GetOrderUseCase
Objetivo: Implementar el caso de uso de consulta de Order, incluyendo lines e history, sin modificar estado ni tocar stock.
Tipo: aplicacion
Rama sugerida: feature/get-order-use-case
Worktree sugerido: worktrees/fulvis-get-order-use-case
```

---

## Nota de Orden

Esta tarea debe ejecutarse despues de `008-implement-get-item-stock-use-case.md` en estado `Accepted`.

Esta tarea implementa una consulta pura de aplicacion. No implementa adaptadores REST, persistencia concreta, cache, observabilidad ni contrato OpenAPI.

---

## Contexto Obligatorio

El agente debe leer antes de trabajar:

```text
specs/CONVENTIONS.md
specs/REQUIREMENTS.md
specs/DOMAIN.md
specs/APPLICATION.md
specs/API.md
specs/QUALITY_GATES.md
specs/SPEC_COVERAGE.md
specs/architecture/architecture.spec.md
specs/use-cases/get-order.spec.md
specs/architecture/project-structure.spec.md
```

---

## Fuentes de Verdad Aplicables

```text
Domain:
- Order
- OrderLine
- OrderHistory

Application:
- OrderRepositoryPort
- GetOrderUseCase

API:
- GET /orders/{orderId}

Specs:
- specs/use-cases/get-order.spec.md
- specs/architecture/architecture.spec.md
- specs/architecture/project-structure.spec.md

Quality gates:
- Casos de uso
- Responsabilidad de Order
- API REST
- Limites de dominio
```

---

## Alcance

### Puede modificar

```text
- src/main/.../application/**
- src/test/.../application/**
- src/main/.../domain/** solo si necesita usar tipos ya definidos sin cambiar decisiones
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
- Modificar Order
- Modificar Item
- Implementar transiciones
- Implementar acciones de stock
```

---

## Restricciones

```text
- Respetar vocabulario tecnico en ingles definido en CONVENTIONS.md
- No modificar ADRs, specs ni quality gates
- GetOrderUseCase debe depender de OrderRepositoryPort
- GetOrderUseCase no debe depender de adaptadores concretos
- GetOrderUseCase no debe modificar Order
- GetOrderUseCase no debe tocar Item
- GetOrderUseCase no debe resolver transiciones
- GetOrderUseCase no debe ejecutar acciones de stock
- traceId no debe entrar en Order ni OrderHistory
- Marcar tensiones entre documentos antes de implementar
```

---

## Specs que Debe Satisfacer

```text
- specs/use-cases/get-order.spec.md
- specs/architecture/architecture.spec.md
- specs/architecture/project-structure.spec.md
```

---

## Quality Gates Aplicables

```text
- La consulta no modifica Order
- La consulta no modifica Item
- La aplicacion depende de puertos
- No hay dependencias de REST, JPA o repositorios concretos en aplicacion
- OrderNotFound se produce desde aplicacion cuando no existe el Order solicitado
- La respuesta incluye lines
- La respuesta incluye history
```

---

## Criterio de Done

La tarea se considera terminada cuando:

```text
- Existe GetOrderUseCase
- Existe resultado de aplicacion para Order si aplica
- GetOrderUseCase carga Order por OrderRepositoryPort
- GetOrderUseCase devuelve orderId, status, createdAt, updatedAt, lines e history
- GetOrderUseCase falla con OrderNotFound si no existe el Order
- GetOrderUseCase no modifica Order
- GetOrderUseCase no toca Item
- GetOrderUseCase no resuelve transiciones
- No hay dependencias de REST, JPA o repositorios concretos en aplicacion
- Existen unit tests de aplicacion en formato Given-When-Then
- Los tests cubren pedido existente, pedido inexistente, lines e history
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
- specs/use-cases/get-order.spec.md
- specs/architecture/architecture.spec.md
- specs/APPLICATION.md
- specs/API.md
- specs/QUALITY_GATES.md
- ausencia de cambios en specs/**
```

La revision humana debe centrarse en que la consulta sea pura, dependa de puertos y no mezcle lectura con transiciones o stock.
