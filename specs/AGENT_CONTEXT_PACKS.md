# AGENT_CONTEXT_PACKS.md - Fulvis

**Estado:** Fase 1  
**Ultima revision:** 2026-08-19

---

## Proposito

Este documento define como preparar el contexto que recibe un agente antes de ejecutar una task.

El objetivo es evitar dos fallos opuestos:

- dar poco contexto y provocar decisiones inventadas
- dar demasiado contexto y diluir la instruccion relevante

Un context pack es el conjunto minimo suficiente de documentos, specs, ADRs y constraints que un agente necesita para ejecutar una task concreta.

---

## Principio

El agente no debe recibir contexto al azar.

Debe recibir:

```text
contexto comun + contexto especifico de la task + restricciones de ejecucion
```

La task sigue siendo la fuente principal de instruccion operativa.

---

## Context Pack Base

Toda ejecucion de agent task debe incluir:

```text
specs/CONVENTIONS.md
specs/AGENT_BACKLOG_GOVERNANCE.md
specs/AGENT_TASK_EXECUTION.md
specs/AGENT_RUN_REPORT_TEMPLATE.md
specs/AGENT_TOOLCHAIN.md
specs/AGENT_ORCHESTRATION_PLAYBOOK.md
specs/AGENT_RESOURCE_ACCOUNTING.md
specs/QUALITY_GATES.md
specs/SPEC_COVERAGE.md
```

Si la ejecucion implica revisar readiness:

```text
specs/AGENT_READINESS_REVIEW.md
```

Si la ejecucion implica crear o modificar agent tasks:

```text
specs/AGENT_TASK_TEMPLATE.md
specs/architecture/agent-backlog-governance.spec.md
```

---

## Context Packs Por Tipo de Task

### Documentacion

```text
specs/CONVENTIONS.md
specs/AGENT_BACKLOG_GOVERNANCE.md
specs/AGENT_TASK_TEMPLATE.md
specs/SPEC_COVERAGE.md
specs/architecture/agent-backlog-governance.spec.md
```

### Dominio

```text
specs/REQUIREMENTS.md
specs/DOMAIN.md
specs/STATE_MACHINE.md
specs/CONVENTIONS.md
specs/QUALITY_GATES.md
specs/architecture/architecture.spec.md
specs/use-cases/transition-order.spec.md
specs/adr/ADR-003-modelado-transiciones-pedido.md
```

### Aplicacion

```text
specs/REQUIREMENTS.md
specs/DOMAIN.md
specs/STATE_MACHINE.md
specs/APPLICATION.md
specs/QUALITY_GATES.md
specs/architecture/architecture.spec.md
specs/use-cases/create-order.spec.md
specs/use-cases/transition-order.spec.md
specs/use-cases/get-order.spec.md
specs/use-cases/get-item-stock.spec.md
specs/adr/ADR-001-concurrencia-reserva-stock.md
specs/adr/ADR-002-concurrencia-transiciones-estado.md
specs/adr/ADR-003-modelado-transiciones-pedido.md
specs/adr/ADR-005-limite-transaccional-aplicacion.md
```

### API

```text
specs/API.md
specs/APPLICATION.md
specs/STATE_MACHINE.md
specs/CONVENTIONS.md
specs/QUALITY_GATES.md
specs/architecture/architecture.spec.md
specs/architecture/observability.spec.md
specs/architecture/project-structure.spec.md
specs/use-cases/create-order.spec.md
specs/use-cases/transition-order.spec.md
specs/use-cases/get-order.spec.md
specs/use-cases/get-item-stock.spec.md
```

### Persistencia

```text
specs/DOMAIN.md
specs/APPLICATION.md
specs/QUALITY_GATES.md
specs/architecture/architecture.spec.md
specs/architecture/project-structure.spec.md
specs/architecture/persistence-concurrency.spec.md
specs/adr/ADR-001-concurrencia-reserva-stock.md
specs/adr/ADR-002-concurrencia-transiciones-estado.md
specs/adr/ADR-005-limite-transaccional-aplicacion.md
```

### Testing

```text
specs/REQUIREMENTS.md
specs/DOMAIN.md
specs/APPLICATION.md
specs/API.md
specs/QUALITY_GATES.md
specs/architecture/*
specs/adr/*
```

### CI y Version Control

```text
specs/CONVENTIONS.md
specs/QUALITY_GATES.md
specs/SPEC_COVERAGE.md
specs/architecture/version-control.spec.md
specs/architecture/project-structure.spec.md
specs/AGENT_RUN_REPORT_TEMPLATE.md
```

---

## Reglas de Construccion

Un context pack debe:

```text
- incluir siempre la task completa
- incluir siempre el context pack base
- incluir documentos especificos por tipo de task
- incluir documentos listados en Contexto Obligatorio de la task
- incluir ADRs citadas directa o indirectamente por la task
- incluir specs que la task debe satisfacer
```

Un context pack no debe:

```text
- omitir documentos obligatorios de la task
- incluir archivos de implementacion no relacionados si no son necesarios
- ocultar tensiones conocidas
- reemplazar la lectura de fuentes de verdad por resumenes ambiguos
```

---

## Orden de Lectura Recomendado

```text
1. Task
2. CONVENTIONS.md
3. AGENT_BACKLOG_GOVERNANCE.md
4. AGENT_TASK_EXECUTION.md
5. Fuentes de verdad de negocio o arquitectura
6. ADRs aplicables
7. Specs aplicables
8. QUALITY_GATES.md
9. SPEC_COVERAGE.md
10. AGENT_RUN_REPORT_TEMPLATE.md
```

---

## Contexto y Riesgo de Cascada

Si el context pack no puede construirse completo:

```text
- no ejecutar la task
- proponer Blocked o Needs Revision
- reportar documentos faltantes
- reportar riesgo de cascada
```

Si aparecen contradicciones entre documentos:

```text
- no resolver por intuicion
- marcar tension
- detener ejecucion si afecta la decision principal
```

---

## Nota Para Harness

Un harness puede usar este documento como algoritmo:

```text
1. Leer task
2. Identificar tipo de task
3. Cargar context pack base
4. Cargar context pack por tipo
5. Cargar Contexto Obligatorio de la task
6. Verificar existencia de todos los documentos
7. Detectar referencias faltantes
8. Entregar contexto al agente
9. Registrar docs leidos en run report
10. Registrar metricas de contexto segun AGENT_RESOURCE_ACCOUNTING.md
```
