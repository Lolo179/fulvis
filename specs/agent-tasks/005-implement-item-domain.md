# Agent Task 005 - Implement Item Domain

**Estado:** Accepted  
**Ultima revision:** 2026-08-21

---

## Task

```text
Nombre: Implement Item domain
Objetivo: Implementar el agregado Item y sus invariantes de inventario sin dependencias de Order, infraestructura ni persistencia.
Tipo: dominio
Rama sugerida: feature/item-domain
Worktree sugerido: worktrees/fulvis-item-domain
```

---

## Nota de Orden

Esta tarea debe ejecutarse despues de `004-implement-order-state-machine.md` en estado `Accepted`.

Esta tarea implementa solo el agregado independiente `Item`. La coordinacion entre `Order` e `Item`, la transaccion, los puertos y el locking pesimista real quedan fuera de alcance y pertenecen a tareas posteriores de aplicacion y persistencia.

---

## Contexto Obligatorio

El agente debe leer antes de trabajar:

```text
specs/CONVENTIONS.md
specs/REQUIREMENTS.md
specs/DOMAIN.md
specs/QUALITY_GATES.md
specs/SPEC_COVERAGE.md
specs/adr/ADR-001-concurrencia-reserva-stock.md
specs/architecture/architecture.spec.md
specs/use-cases/create-order.spec.md
specs/use-cases/transition-order.spec.md
specs/use-cases/get-item-stock.spec.md
```

---

## Fuentes de Verdad Aplicables

```text
Requirements:
- REQ-S01
- REQ-S02
- REQ-S03
- REQ-S04
- RN-01
- RN-02
- RN-03

Domain:
- Item
- stockTotal
- stockReserved
- stockAvailable
- reserve(quantity)
- release(quantity)
- decrement(quantity)

ADRs:
- ADR-001

Specs:
- specs/architecture/architecture.spec.md
- specs/use-cases/create-order.spec.md
- specs/use-cases/transition-order.spec.md
- specs/use-cases/get-item-stock.spec.md

Quality gates:
- Responsabilidad de Item
- Limites de dominio
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
- Implementar Order
- Implementar OrderLine
- Implementar OrderStateMachine
- Implementar repositorios
- Implementar locking pesimista real
- Implementar persistencia JPA
- Implementar controladores REST
```

---

## Restricciones

```text
- Respetar vocabulario tecnico en ingles definido en CONVENTIONS.md
- No introducir dependencias de Spring en dominio
- No introducir dependencias de JPA en dominio
- No introducir dependencias HTTP en dominio
- Item no debe conocer Order
- Item no debe conocer OrderLine
- Item no debe conocer OrderHistory
- Order puede referenciar Item solo por itemId dentro de OrderLine, pero no como objeto Item
- No implementar locking real dentro del agregado Item
- No modificar ADRs, specs ni quality gates
- Marcar tensiones entre documentos antes de implementar
```

---

## Specs que Debe Satisfacer

```text
- specs/architecture/architecture.spec.md
- specs/use-cases/create-order.spec.md
- specs/use-cases/transition-order.spec.md
- specs/use-cases/get-item-stock.spec.md
```

---

## Quality Gates Aplicables

```text
- Item protege stockTotal >= 0
- Item protege stockReserved >= 0
- Item protege stockTotal >= stockReserved
- Item calcula stockAvailable como valor derivado
- reserve(quantity) falla si stockAvailable < quantity
- release(quantity) no permite liberar mas de lo reservado
- decrement(quantity) reduce stockTotal y stockReserved de forma consistente
- Item no depende de Order
- Dominio no depende de infraestructura
```

---

## Criterio de Done

La tarea se considera terminada cuando:

```text
- Existe Item en dominio
- Item expone stockAvailable como valor derivado
- Item implementa reserve(quantity)
- Item implementa release(quantity)
- Item implementa decrement(quantity)
- Item preserva invariantes de stock
- Item no referencia Order ni OrderLine
- No hay dependencias de Spring, JPA o HTTP en dominio
- Existen unit tests de dominio en formato Given-When-Then
- Los tests cubren reserva valida, stock insuficiente, liberacion valida, liberacion invalida, decremento valido y decremento invalido
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
- specs/use-cases/create-order.spec.md
- specs/use-cases/transition-order.spec.md
- specs/use-cases/get-item-stock.spec.md
- specs/DOMAIN.md
- specs/QUALITY_GATES.md
- ausencia de cambios en specs/**
```

La revision humana debe centrarse en invariantes de inventario, independencia entre agregados y cobertura de errores, no en revisar linea por linea toda la implementacion.
