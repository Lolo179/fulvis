# Agent Task 001 - Migrate Docs to Specs Structure

**Estado:** Accepted  
**Ultima revision:** 2026-08-20

---

## Task

```text
Nombre: Migrate docs to specs structure
Objetivo: Migrar la estructura documental provisional `docs/` hacia la estructura objetivo `specs/` definida en ADR-004.
Tipo: documentacion
Rama sugerida: specs/migrate-to-specs-structure
Worktree sugerido: worktrees/fulvis-specs-structure
```

---

## Contexto Obligatorio

El agente debe leer antes de trabajar:

```text
specs/CONVENTIONS.md
specs/adr/ADR-004-estructura-sdd-harness.md
specs/AGENT_TASK_TEMPLATE.md
specs/AGENT_BACKLOG_GOVERNANCE.md
specs/AGENT_READINESS_REVIEW.md
specs/AGENT_TASK_EXECUTION.md
specs/AGENT_RUN_REPORT_TEMPLATE.md
specs/AGENT_CONTEXT_PACKS.md
specs/AGENT_TOOLCHAIN.md
specs/AGENT_ORCHESTRATION_PLAYBOOK.md
specs/AGENT_RESOURCE_ACCOUNTING.md
specs/SPEC_COVERAGE.md
```

---

## Fuentes de Verdad Aplicables

```text
ADRs:
- ADR-004

Conventions:
- Organizacion Documental
- Jerarquia de Fuentes de Verdad
- Control de Versiones

Quality gates:
- Vocabulario tecnico
- Version control and agent workflow
```

---

## Alcance

### Puede modificar

```text
- specs/**
- .github/agents/** solo para actualizar referencias de `docs/` a `specs/`
```

### No puede modificar

```text
- src/**
- pom.xml
- codigo de aplicacion
- codigo de tests
```

### Fuera de alcance

```text
- Cambiar decisiones arquitectonicas
- Reescribir contenido conceptual
- Crear implementacion Java
- Crear scaffold de proyecto
```

---

## Migracion Objetivo

Estructura destino:

```text
specs/
  REQUIREMENTS.md
  DOMAIN.md
  STATE_MACHINE.md
  APPLICATION.md
  API.md
  QUALITY_GATES.md
  SPEC_COVERAGE.md
  AGENT_TASK_TEMPLATE.md
  AGENT_BACKLOG_GOVERNANCE.md
  AGENT_READINESS_REVIEW.md
  AGENT_TASK_EXECUTION.md
  AGENT_RUN_REPORT_TEMPLATE.md
  AGENT_CONTEXT_PACKS.md
  AGENT_TOOLCHAIN.md
  AGENT_ORCHESTRATION_PLAYBOOK.md
  AGENT_RESOURCE_ACCOUNTING.md
  adr/
  use-cases/
  architecture/
  agent-profiles/
  agent-briefs/
  agent-tasks/
```

Mapeo esperado:

```text
docs/REQUIREMENTS.md              -> specs/REQUIREMENTS.md
docs/DOMAIN.md                    -> specs/DOMAIN.md
docs/STATE_MACHINE.md             -> specs/STATE_MACHINE.md
docs/APPLICATION.md               -> specs/APPLICATION.md
docs/API.md                       -> specs/API.md
docs/QUALITY_GATES.md             -> specs/QUALITY_GATES.md
docs/SPEC_COVERAGE.md             -> specs/SPEC_COVERAGE.md
docs/AGENT_TASK_TEMPLATE.md       -> specs/AGENT_TASK_TEMPLATE.md
docs/AGENT_BACKLOG_GOVERNANCE.md  -> specs/AGENT_BACKLOG_GOVERNANCE.md
docs/AGENT_READINESS_REVIEW.md    -> specs/AGENT_READINESS_REVIEW.md
docs/AGENT_TASK_EXECUTION.md      -> specs/AGENT_TASK_EXECUTION.md
docs/AGENT_RUN_REPORT_TEMPLATE.md -> specs/AGENT_RUN_REPORT_TEMPLATE.md
docs/AGENT_CONTEXT_PACKS.md       -> specs/AGENT_CONTEXT_PACKS.md
docs/AGENT_TOOLCHAIN.md           -> specs/AGENT_TOOLCHAIN.md
docs/AGENT_ORCHESTRATION_PLAYBOOK.md -> specs/AGENT_ORCHESTRATION_PLAYBOOK.md
docs/AGENT_RESOURCE_ACCOUNTING.md -> specs/AGENT_RESOURCE_ACCOUNTING.md
docs/adr/*                        -> specs/adr/*
docs/specs/create-order.spec.md   -> specs/use-cases/create-order.spec.md
docs/specs/transition-order.spec.md -> specs/use-cases/transition-order.spec.md
docs/specs/get-item-stock.spec.md -> specs/use-cases/get-item-stock.spec.md
docs/specs/get-order.spec.md      -> specs/use-cases/get-order.spec.md
docs/specs/observability.spec.md  -> specs/architecture/observability.spec.md
docs/specs/architecture.spec.md   -> specs/architecture/architecture.spec.md
docs/specs/project-structure.spec.md -> specs/architecture/project-structure.spec.md
docs/specs/persistence-concurrency.spec.md -> specs/architecture/persistence-concurrency.spec.md
docs/specs/version-control.spec.md -> specs/architecture/version-control.spec.md
docs/specs/agent-backlog-governance.spec.md -> specs/architecture/agent-backlog-governance.spec.md
docs/agent-profiles/*             -> specs/agent-profiles/*
docs/agent-briefs/*               -> specs/agent-briefs/*
docs/agent-tasks/*                -> specs/agent-tasks/*
```

Nota:

```text
.github/agents/* no se migra a specs/.
Es un adaptador ejecutable de herramienta y debe permanecer en .github/agents/.
Sus referencias internas deben actualizarse si apuntan a artefactos migrados.
```

---

## Restricciones

```text
- Mantener todo el contenido en ASCII
- Actualizar referencias internas de `docs/` a `specs/`
- No cambiar significado de requisitos, ADRs, specs o quality gates
- No reordenar decisiones sin justificacion
- No modificar el estado de ADRs aceptadas
- Reportar cualquier referencia ambigua o rota
```

---

## Specs que Debe Satisfacer

```text
- specs/architecture/architecture.spec.md
- specs/architecture/version-control.spec.md
- specs/architecture/agent-backlog-governance.spec.md
```

---

## Quality Gates Aplicables

```text
- Vocabulario tecnico
- Specs must not override accepted architecture
- Version control and agent workflow
```

---

## Criterio de Done

La tarea se considera terminada cuando:

```text
- Existe la carpeta `specs/` con la estructura objetivo
- No quedan referencias internas rotas hacia `docs/`
- `CONVENTIONS.md` refleja que `specs/` ya es la estructura actual
- `SPEC_COVERAGE.md` apunta a rutas nuevas
- Las agent tasks apuntan a rutas nuevas
- No hay caracteres no ASCII
- El agente lista archivos movidos y referencias actualizadas
```

---

## Output Esperado del Agente

El agente debe entregar:

```text
Resumen del cambio:

Docs leidos:

Archivos movidos:

Referencias actualizadas:

Referencias ambiguas o no resueltas:

Specs satisfechas:

Quality gates relevantes:

Validaciones ejecutadas:
```

---

## Politica de Revision

El supervisor evaluara la tarea mediante:

```text
- diff de movimientos
- busqueda de referencias a `docs/`
- busqueda de caracteres no ASCII
- comprobacion de estructura final
- lectura de CONVENTIONS.md y ADR-004
```

Esta tarea debe ejecutarse antes de delegar implementaciones de codigo a agentes.
