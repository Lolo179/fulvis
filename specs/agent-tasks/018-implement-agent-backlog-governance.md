# Agent Task 018 - Implement Agent Backlog Governance

**Estado:** Draft  
**Ultima revision:** 2026-08-19

---

## Task

```text
Nombre: Implement Agent Backlog Governance
Objetivo: Incorporar las reglas de AGENT_BACKLOG_GOVERNANCE.md al flujo operativo de agentes, checks y documentacion del proyecto.
Tipo: documentacion
Rama sugerida: specs/agent-backlog-governance
Worktree sugerido: worktrees/fulvis-agent-backlog-governance
```

---

## Contexto Obligatorio

El agente debe leer antes de trabajar:

```text
specs/CONVENTIONS.md
specs/AGENT_TASK_TEMPLATE.md
specs/AGENT_BACKLOG_GOVERNANCE.md
specs/QUALITY_GATES.md
specs/SPEC_COVERAGE.md
specs/architecture/agent-backlog-governance.spec.md
specs/architecture/version-control.spec.md
specs/agent-tasks/*
```

---

## Fuentes de Verdad Aplicables

```text
Governance:
- specs/AGENT_BACKLOG_GOVERNANCE.md

Task format:
- specs/AGENT_TASK_TEMPLATE.md

Conventions:
- specs/CONVENTIONS.md

Version control:
- specs/architecture/version-control.spec.md

Specs:
- specs/architecture/agent-backlog-governance.spec.md

Quality gates:
- specs/QUALITY_GATES.md
```

---

## Alcance

### Puede modificar

```text
- specs/AGENT_BACKLOG_GOVERNANCE.md
- specs/AGENT_TASK_TEMPLATE.md
- specs/CONVENTIONS.md
- specs/QUALITY_GATES.md
- specs/SPEC_COVERAGE.md
- specs/agent-tasks/**
- scripts/**
- README.md
```

### No puede modificar

```text
- reglas de negocio
- ADRs aceptadas salvo decision explicita
- specs funcionales salvo tension documentada
- src/main/** salvo que exista implementacion de harness local aprobada
```

### Fuera de alcance

```text
- Implementar funcionalidades de Fulvis
- Cambiar arquitectura de dominio
- Cambiar API REST
- Cambiar persistencia
- Ejecutar agentes reales
- Integrar herramientas externas sin autorizacion explicita
```

---

## Restricciones

```text
- Respetar vocabulario tecnico en ingles definido en CONVENTIONS.md
- No modificar ADRs aceptadas sin decision explicita
- No cambiar el orden de tasks sin actualizar referencias cruzadas
- No marcar tasks como Ready si no cumplen criterios de governance
- No ocultar tasks Blocked o Needs Revision
- No permitir que una task dependa implicitamente de una task posterior
- Marcar tensiones entre documentos antes de implementar
```

---

## Checks Esperados

Si existe soporte de scripts o checks documentales, implementar verificaciones para:

```text
- cada task tiene Estado
- cada task usa un estado permitido
- cada task tiene Contexto Obligatorio
- cada task tiene Alcance
- cada task tiene Restricciones
- cada task tiene Criterio de Done
- cada task tiene Output Esperado del Agente
- nombres de archivo y titulos coinciden en numero
- no existen referencias a task filenames inexistentes
- no existen dependencias numericamente posteriores no marcadas como excepcion
```

Si no existe soporte de scripts aun, documentar estos checks como pendientes en `SPEC_COVERAGE.md` o `QUALITY_GATES.md`.

---

## Specs que Debe Satisfacer

```text
- specs/architecture/agent-backlog-governance.spec.md
- specs/architecture/version-control.spec.md
```

---

## Quality Gates Aplicables

```text
- Version control and agent workflow
- Evaluacion por specs, quality gates y diffs
- Tareas delegadas a agentes siguen AGENT_TASK_TEMPLATE.md
- No se continua con dependientes si hay Blocked o Needs Revision
```

---

## Criterio de Done

La tarea se considera terminada cuando:

```text
- AGENT_BACKLOG_GOVERNANCE.md esta enlazado desde CONVENTIONS.md
- AGENT_TASK_TEMPLATE.md incluye governance en contexto obligatorio
- AGENT_TASK_TEMPLATE.md incluye estado final, reintentos y riesgo de cascada en output
- Existe mecanismo documental o script para revisar consistencia de agent tasks
- Las agent tasks existentes se revisan contra estados permitidos
- Las referencias cruzadas entre agent tasks son validas
- SPEC_COVERAGE.md se actualiza si cambia cobertura
- El agente reporta gaps, riesgos o tensiones detectadas
```

---

## Output Esperado del Agente

El agente debe entregar:

```text
Resumen del cambio:

Task ejecutada:

Estado final propuesto:

Docs leidos:

Specs satisfechas:

Quality gates relevantes:

Archivos modificados:

Validaciones ejecutadas:

Reintentos realizados:

Tensiones detectadas:

Gaps restantes:

Riesgo de cascada:
```

---

## Politica de Revision

El supervisor evaluara la tarea mediante:

```text
- diff
- AGENT_BACKLOG_GOVERNANCE.md
- AGENT_TASK_TEMPLATE.md
- CONVENTIONS.md
- listado de agent tasks
- validacion de referencias cruzadas si existe
```

La revision humana debe centrarse en que el backlog sea seguro de ejecutar por agentes: ordenado, recuperable ante fallo y resistente a errores en cascada.
