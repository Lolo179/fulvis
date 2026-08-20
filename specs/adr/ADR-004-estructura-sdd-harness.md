# ADR-004 - Estructura documental para SDD y harness

**Estado:** Aceptado  
**Fecha:** 2026-08-18  
**Autor:** Luis M.

---

## Contexto

Fulvis empezo usando `docs/` como carpeta general para requisitos, dominio, ADRs, specs, quality gates y tareas de agentes.

Conforme el proyecto avanza hacia un enfoque SDD, harness engineering y supervision de agentes, estos archivos dejan de ser documentacion pasiva. Funcionan como artefactos activos de direccion del desarrollo:

- fuentes de verdad
- specs de comportamiento
- quality gates
- matriz de coverage
- tareas delegables a agentes
- decisiones arquitectonicas

---

## Problema

La carpeta `docs/` comunicaba una intencion demasiado generica.

Para trabajo con agentes, CLI, MCP, worktrees y specs, conviene que la estructura indique que estos archivos gobiernan implementacion, verificacion y supervision.

Tambien es necesario evitar ambiguedad entre:

- documentacion explicativa
- specs que dirigen implementacion
- ADRs que gobiernan decisiones
- agent tasks que operan como ordenes de trabajo

---

## Alternativas consideradas

| Alternativa | Motivo de descarte |
|---|---|
| Mantener todo en `docs/` | Es simple, pero menos expresivo para SDD/harness y agentes |
| Crear carpetas raiz separadas (`adr/`, `tasks/`, `quality/`) | Puede dispersar contexto y complicar la carga inicial para agentes |
| Usar `specs/` como raiz SDD | Hace explicita la intencion de fuente de verdad y mantiene cohesion entre artefactos |

---

## Decision

Fulvis adoptara `specs/` como estructura objetivo para artefactos SDD/harness.

Estructura objetivo:

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

La estructura fisica bajo `docs/` se mantuvo temporalmente para no romper referencias durante la fase de modelado.

La migracion fisica a `specs/` queda implementada como parte de `001-migrate-docs-to-specs-structure.md`.

---

## Consecuencias

- El vocabulario del proyecto queda mas alineado con SDD y harness engineering.
- Los agentes podran interpretar estos artefactos como entradas activas del proceso, no solo documentacion auxiliar.
- Se requirio actualizar referencias internas al migrar de `docs/` a `specs/`.
- Las specs se reorganizaron en carpetas mas claras, por ejemplo `specs/use-cases/` y `specs/architecture/`.
- Los perfiles y briefs de agentes deberan organizarse en `specs/agent-profiles/` y `specs/agent-briefs/`.
- `CONVENTIONS.md` debe reflejar `specs/` como estructura actual.

---

## Relacion con otras decisiones

- `CONVENTIONS.md` define la jerarquia de fuentes de verdad y la estructura objetivo.
- `QUALITY_GATES.md` define restricciones globales de aceptacion.
- `SPEC_COVERAGE.md` traza requisitos y decisiones hacia specs y gates.
- `version-control.spec.md` prepara el trabajo con Git, GitHub, PRs y worktrees.

---

## Precision posterior

Durante la definicion del harness se agregaron artefactos operativos de supervision:

```text
AGENT_BACKLOG_GOVERNANCE.md
AGENT_READINESS_REVIEW.md
AGENT_TASK_EXECUTION.md
AGENT_RUN_REPORT_TEMPLATE.md
AGENT_CONTEXT_PACKS.md
AGENT_TOOLCHAIN.md
AGENT_ORCHESTRATION_PLAYBOOK.md
AGENT_RESOURCE_ACCOUNTING.md
```

Estos documentos no cambian la decision principal de esta ADR. Solo completan el conjunto de artefactos que debe migrarse junto con el resto de fuentes de verdad cuando se consolide `specs/`.
