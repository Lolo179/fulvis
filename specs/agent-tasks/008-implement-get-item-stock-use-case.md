# Agent Task 008 - Implement GetItemStockUseCase

**Estado:** Draft  
**Ultima revision:** 2026-08-18

---

## Task

```text
Nombre: Implement GetItemStockUseCase
Objetivo: Implementar el caso de uso de consulta de stock de Item sin modificar estado.
Tipo: aplicacion
Rama sugerida: feature/get-item-stock-use-case
Worktree sugerido: worktrees/fulvis-get-item-stock-use-case
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
specs/architecture/architecture.spec.md
specs/use-cases/get-item-stock.spec.md
specs/architecture/project-structure.spec.md
```

---

## Fuentes de Verdad Aplicables

```text
Requirements:
- REQ-S01
- RN-01

Domain:
- Item
- stockTotal
- stockReserved
- stockAvailable

Application:
- ItemRepositoryPort
- GetItemStockUseCase

API:
- GET /items/{itemId}/stock

Specs:
- specs/use-cases/get-item-stock.spec.md
- specs/architecture/architecture.spec.md
- specs/architecture/project-structure.spec.md

Quality gates:
- Responsabilidad de Item
- Casos de uso
- API REST
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
- Modificar Item
- Modificar Order
- Modificar reglas de stock
```

---

## Restricciones

```text
- Respetar vocabulario tecnico en ingles definido en CONVENTIONS.md
- No modificar ADRs, specs ni quality gates
- GetItemStockUseCase debe depender de ItemRepositoryPort
- GetItemStockUseCase no debe depender de adaptadores concretos
- GetItemStockUseCase no debe modificar Item
- GetItemStockUseCase no debe tocar Order
- stockAvailable debe obtenerse como valor derivado de Item
- traceId no debe entrar en Item ni en el resultado de dominio
- Marcar tensiones entre documentos antes de implementar
```

---

## Specs que Debe Satisfacer

```text
- specs/use-cases/get-item-stock.spec.md
- specs/architecture/architecture.spec.md
- specs/architecture/project-structure.spec.md
```

---

## Quality Gates Aplicables

```text
- La consulta no modifica Item
- Item calcula stockAvailable como valor derivado
- La aplicacion depende de puertos
- No hay dependencias de REST, JPA o repositorios concretos en aplicacion
- ItemNotFound se produce desde aplicacion cuando no existe el Item solicitado
```

---

## Criterio de Done

La tarea se considera terminada cuando:

```text
- Existe GetItemStockUseCase
- Existe resultado de aplicacion para stock si aplica
- GetItemStockUseCase carga Item por ItemRepositoryPort
- GetItemStockUseCase devuelve itemId, stockTotal, stockReserved y stockAvailable
- GetItemStockUseCase falla con ItemNotFound si no existe el Item
- GetItemStockUseCase no modifica Item
- GetItemStockUseCase no toca Order
- No hay dependencias de REST, JPA o repositorios concretos en aplicacion
- Existen unit tests de aplicacion en formato Given-When-Then
- Los tests cubren item existente, item inexistente y calculo de stockAvailable
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
- specs/use-cases/get-item-stock.spec.md
- specs/architecture/architecture.spec.md
- specs/APPLICATION.md
- specs/API.md
- specs/QUALITY_GATES.md
- ausencia de cambios en specs/**
```

La revision humana debe centrarse en que la consulta sea pura, dependa de puertos y respete el calculo derivado de stock, no en revisar linea por linea toda la implementacion.
