# AGENT_TASK_TEMPLATE.md - Fulvis

**Estado:** Fase 1  
**Ultima revision:** 2026-08-18

---

## Proposito

Plantilla para definir tareas de agentes en Fulvis.

El objetivo es que cada agente trabaje con contexto suficiente, limites claros, specs verificables y criterios de salida explicitos.

---

## Task

```text
Nombre:
Objetivo:
Tipo: documentacion | dominio | aplicacion | API | persistencia | testing | refactor | experimento
Rama sugerida:
Worktree sugerido:
```

---

## Contexto Obligatorio

El agente debe leer antes de trabajar:

```text
specs/CONVENTIONS.md
specs/REQUIREMENTS.md
specs/DOMAIN.md
specs/STATE_MACHINE.md
specs/APPLICATION.md
specs/API.md
specs/QUALITY_GATES.md
specs/SPEC_COVERAGE.md
specs/AGENT_BACKLOG_GOVERNANCE.md
specs/AGENT_TASK_EXECUTION.md
specs/AGENT_RUN_REPORT_TEMPLATE.md
specs/AGENT_CONTEXT_PACKS.md
specs/adr/*
specs/architecture/*
specs/use-cases/*
```

Para tareas pequenas, el supervisor puede limitar el contexto, pero debe indicar explicitamente que documentos aplican.

---

## Fuentes de Verdad Aplicables

```text
Requirements:
- 

Domain:
- 

State machine:
- 

ADRs:
- 

Application:
- 

API:
- 

Specs:
- 

Quality gates:
- 
```

---

## Alcance

### Puede modificar

```text
- 
```

### No puede modificar

```text
- 
```

### Fuera de alcance

```text
- 
```

---

## Restricciones

```text
- Respetar vocabulario tecnico en ingles definido en CONVENTIONS.md
- No introducir estados, eventos, errores o agregados no documentados
- No modificar ADRs aceptadas sin decision explicita
- No mezclar cambios conceptuales con cambios mecanicos sin explicarlo
- No mover reglas de negocio a adaptadores externos
- No introducir abstracciones nuevas sin justificar necesidad
- Marcar tensiones entre documentos antes de implementar
```

---

## Specs que Debe Satisfacer

```text
- specs/architecture/
- specs/use-cases/
```

---

## Quality Gates Aplicables

```text
- 
```

---

## Criterio de Done

La tarea se considera terminada cuando:

```text
- El cambio cumple las specs aplicables
- El cambio respeta quality gates aplicables
- No contradice fuentes de verdad de mayor alcance
- La matriz SPEC_COVERAGE.md queda actualizada si cambia cobertura
- Las ADRs quedan actualizadas si se toma una decision arquitectonica nueva
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

Tensiones detectadas:

Gaps restantes:

Validaciones ejecutadas:

Reintentos realizados:

Riesgo de cascada:
```

---

## Politica de Revision

El supervisor evaluara la tarea principalmente mediante:

```text
- diff
- specs aplicables
- quality gates
- coverage
- tests/checks disponibles
- riesgos declarados
```

La revision humana no debe depender de leer linea por linea todo el codigo generado. Debe centrarse en decisiones, desviaciones, gaps y cobertura insuficiente.

---

## Ejemplo Breve

```text
Nombre: Implementar Order domain
Objetivo: Crear el modelo de dominio para Order, OrderLine, OrderHistory y transiciones internas.
Tipo: dominio
Rama sugerida: feature/order-domain
Worktree sugerido: worktrees/fulvis-order-domain

Specs:
- specs/architecture/architecture.spec.md
- specs/use-cases/transition-order.spec.md

Quality gates:
- Dominio aislado
- Order no expone setStatus
- Order no opera stock
- Order crea OrderHistory

No puede modificar:
- API.md
- ADRs aceptadas
- Adaptadores de persistencia
```
