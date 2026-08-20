# Agent Task 014 - Implement Transaction Boundaries and Wiring

**Estado:** Draft  
**Ultima revision:** 2026-08-19

---

## Task

```text
Nombre: Implement Transaction Boundaries and Wiring
Objetivo: Configurar el wiring de casos de uso, puertos, adaptadores y limites transaccionales segun ADR-005.
Tipo: aplicacion
Rama sugerida: feature/transaction-boundaries-wiring
Worktree sugerido: worktrees/fulvis-transaction-boundaries-wiring
```

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
specs/adr/ADR-001-concurrencia-reserva-stock.md
specs/adr/ADR-002-concurrencia-transiciones-estado.md
specs/adr/ADR-003-modelado-transiciones-pedido.md
specs/adr/ADR-005-limite-transaccional-aplicacion.md
specs/architecture/architecture.spec.md
specs/architecture/project-structure.spec.md
specs/architecture/persistence-concurrency.spec.md
specs/agent-tasks/006-implement-create-order-use-case.md
specs/agent-tasks/007-implement-transition-order-use-case.md
specs/agent-tasks/012-implement-rest-adapters.md
specs/agent-tasks/013-implement-persistence-adapters.md
```

---

## Fuentes de Verdad Aplicables

```text
ADRs:
- ADR-001 locking pesimista para reserva de stock
- ADR-002 locking optimista para transiciones de Order
- ADR-003 Application Service orquesta Order e Item
- ADR-005 limite transaccional en aplicacion

Application:
- CreateOrderUseCase
- TransitionOrderUseCase
- GetOrderUseCase
- GetItemStockUseCase
- OrderRepositoryPort
- ItemRepositoryPort

Architecture:
- Application depends on ports, not adapters
- REST adapters must not contain business decisions
- Domain must not depend on external adapters

Specs:
- specs/architecture/architecture.spec.md
- specs/architecture/project-structure.spec.md
- specs/architecture/persistence-concurrency.spec.md

Quality gates:
- Casos de uso
- API REST
- Idempotencia y concurrencia
- Limites de dominio
```

---

## Alcance

### Puede modificar

```text
- src/main/.../application/**
- src/main/.../configuration/**
- src/main/.../adapters/**
- src/test/.../application/**
- src/test/.../configuration/**
- src/test/.../adapters/**
```

### No puede modificar

```text
- specs/**
- src/main/.../domain/**
- src/main/resources/openapi/**
```

### Fuera de alcance

```text
- Cambiar reglas de negocio
- Cambiar estados, eventos o transiciones
- Cambiar contrato REST u OpenAPI
- Implementar nuevos endpoints
- Introducir mensajeria
- Introducir un TransactionRunner propio en Fase 1
- Mover logica de negocio a controladores o repositorios
```

---

## Restricciones

```text
- Respetar vocabulario tecnico en ingles definido en CONVENTIONS.md
- No modificar ADRs, specs ni quality gates
- Aplicar ADR-005 como decision aceptada
- CreateOrderUseCase debe ejecutarse dentro de una transaccion de escritura
- TransitionOrderUseCase debe ejecutarse dentro de una transaccion de escritura
- GetOrderUseCase no debe requerir transaccion de escritura
- GetItemStockUseCase no debe requerir transaccion de escritura
- @Transactional no debe aparecer en dominio
- @Transactional no debe aparecer en controladores REST
- Los controladores REST no deben abrir transacciones
- Los repositorios concretos no deben definir la unidad completa de negocio
- Los casos de uso no deben depender de HTTP, DTOs REST ni ResponseEntity
- Marcar tensiones entre documentos antes de implementar
```

---

## Wiring Esperado

```text
REST adapter -> use case de aplicacion -> ports -> persistence adapters
```

El wiring debe permitir que:

```text
- REST invoque casos de uso sin conocer repositorios concretos
- Casos de uso dependan de puertos
- Adaptadores de persistencia implementen puertos
- Transacciones envuelvan la orquestacion completa del caso de uso
```

---

## Limites Transaccionales Esperados

```text
CreateOrderUseCase:
- reserva Items
- crea Order
- crea OrderHistory inicial
- persiste Order e Items
- todo dentro de una unica transaccion

TransitionOrderUseCase:
- carga Order
- resuelve intencion
- carga Items cuando hay accion de stock
- aplica accion de stock
- aplica transicion e historial
- persiste Order e Items
- todo dentro de una unica transaccion

GetOrderUseCase:
- lectura
- no modifica estado
- no requiere transaccion de escritura

GetItemStockUseCase:
- lectura
- no modifica stock
- no requiere transaccion de escritura
```

---

## Specs que Debe Satisfacer

```text
- specs/architecture/architecture.spec.md
- specs/architecture/project-structure.spec.md
- specs/architecture/persistence-concurrency.spec.md
```

---

## Quality Gates Aplicables

```text
- Operaciones que afectan Order e Item usan una unica transaccion local
- Fallos de persistencia hacen rollback completo
- La aplicacion depende de puertos
- Los adaptadores implementan puertos
- Los controladores REST no contienen decisiones de negocio
- El dominio no conoce Spring, JPA ni HTTP
- Reserva de stock usa locking pesimista dentro de transaccion
- Transiciones de Order usan locking optimista
```

---

## Criterio de Done

La tarea se considera terminada cuando:

```text
- CreateOrderUseCase tiene limite transaccional de escritura
- TransitionOrderUseCase tiene limite transaccional de escritura
- Los casos de uso de lectura no abren transacciones de escritura
- @Transactional no aparece en dominio
- @Transactional no aparece en controladores REST
- Los controladores invocan casos de uso, no repositorios
- Los casos de uso dependen de puertos, no adaptadores
- Los adaptadores de persistencia quedan registrados como implementaciones de puertos
- Existe configuracion clara de beans si no se usa autodeteccion
- Existen tests o checks que verifican ausencia de transacciones en REST y dominio si la estructura lo permite
- Existen integration tests de rollback si la persistencia ya existe
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
- integration tests de rollback cuando existan
- checks estructurales de dependencias
- specs/adr/ADR-005-limite-transaccional-aplicacion.md
- specs/APPLICATION.md
- specs/architecture/architecture.spec.md
- specs/architecture/persistence-concurrency.spec.md
- specs/QUALITY_GATES.md
- ausencia de @Transactional en dominio y REST
```

La revision humana debe centrarse en que la transaccion envuelva el caso de uso completo sin convertir controladores, repositorios o dominio en duenos de la unidad de negocio.
