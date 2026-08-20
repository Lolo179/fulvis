# Spec - Agent Backlog Governance

**Estado:** Fase 1  
**Ultima revision:** 2026-08-19

---

## Proposito

Definir comportamientos verificables para gobernar agent tasks, evitar errores en cascada y preparar el backlog para ejecucion mediante agentes, harnesses, tools o skills.

Esta spec no implementa un harness. Define las reglas que una implementacion futura debe poder comprobar.

---

## Escenario 1 - Task con estado valido

**Given**
- Existe una agent task en `specs/agent-tasks/`

**When**
- Se valida su metadata

**Then**
- La task declara un `Estado`
- El estado pertenece a la lista permitida:

```text
Draft
Ready
In Progress
Blocked
Needs Revision
Accepted
Superseded
```

---

## Escenario 2 - Task Ready cumple requisitos minimos

**Given**
- Existe una agent task con `Estado: Ready`

**When**
- Se valida contra `AGENT_BACKLOG_GOVERNANCE.md`

**Then**
- Tiene objetivo claro
- Tiene contexto obligatorio
- Tiene alcance
- Tiene restricciones
- Tiene specs aplicables
- Tiene quality gates aplicables
- Tiene criterio de done
- No depende implicitamente de una task posterior
- No contradice ADRs aceptadas

---

## Escenario 3 - Referencias cruzadas validas

**Given**
- Una agent task referencia otros documentos o agent tasks

**When**
- Se valida el backlog

**Then**
- Toda referencia a `specs/agent-tasks/*.md` apunta a un archivo existente
- Toda referencia a `specs/architecture/*.md` apunta a un archivo existente
- Toda referencia a `specs/adr/*.md` apunta a un archivo existente
- No quedan referencias a filenames antiguos despues de una renumeracion

---

## Escenario 4 - Numero de archivo y titulo coinciden

**Given**
- Existe una agent task con filename numerado

**When**
- Se compara el prefijo numerico del filename con el titulo

**Then**
- El numero del filename coincide con el numero indicado en `# Agent Task NNN`
- El orden numerico representa orden recomendado de ejecucion

---

## Escenario 5 - Politica anti-cascada

**Given**
- Una task depende de otra task previa
- La task previa esta en `Blocked` o `Needs Revision`

**When**
- Un agente o harness intenta ejecutar la task dependiente

**Then**
- La ejecucion se detiene
- Se reporta riesgo de cascada
- No se continua salvo excepcion explicita del supervisor

---

## Escenario 6 - Reintento permitido

**Given**
- Una task falla durante la ejecucion
- El fallo es tecnico y local
- La correccion no cambia arquitectura, negocio, contrato externo ni ADRs

**When**
- El agente aplica una correccion dentro del alcance

**Then**
- Puede reintentar la operacion
- El reintento queda reportado en la salida final
- No se oculta el fallo original
- No se supera `maxRetries = 2`

---

## Escenario 7 - Reintento no permitido

**Given**
- Una task falla durante la ejecucion
- La posible correccion requiere cambiar arquitectura, negocio, contrato externo, ADRs o dependencias de orden

**When**
- El agente evalua el fallo

**Then**
- No aplica la correccion automaticamente
- Propone `Blocked` o `Needs Revision`
- Reporta tension detectada
- Escala al supervisor

---

## Escenario 8 - Salida final completa

**Given**
- Un agente finaliza una task

**When**
- Entrega su output

**Then**
- Incluye resumen del cambio
- Incluye task ejecutada
- Incluye estado final propuesto
- Incluye docs leidos
- Incluye specs satisfechas
- Incluye quality gates relevantes
- Incluye archivos modificados
- Incluye validaciones ejecutadas
- Incluye reintentos realizados
- Incluye tensiones detectadas
- Incluye gaps restantes
- Incluye riesgo de cascada

---

## Escenario 9 - Task Ready sigue protocolo de ejecucion

**Given**
- Existe una agent task en estado `Ready`

**When**
- Un agente o harness la ejecuta

**Then**
- Lee `AGENT_TASK_EXECUTION.md`
- Ejecuta preflight antes de modificar archivos
- Verifica alcance permitido
- Ejecuta validaciones definidas
- Reintenta solo si el fallo es seguro de reintentar
- Reporta estado final propuesto

---

## Escenario 10 - Run report registra evidencia de ejecucion

**Given**
- Un agente finaliza una ejecucion de task

**When**
- Genera su reporte de salida

**Then**
- El reporte es compatible con `AGENT_RUN_REPORT_TEMPLATE.md`
- Incluye task ejecutada
- Incluye estado final propuesto
- Incluye validaciones ejecutadas
- Incluye validaciones no ejecutadas si las hay
- Incluye reintentos realizados
- Incluye tensiones y gaps
- Incluye riesgo de cascada
- Incluye commit base y commit final si existe Git

---

## Escenario 11 - Context pack minimo suficiente

**Given**
- Existe una agent task lista para ejecucion

**When**
- Un harness o supervisor prepara el contexto

**Then**
- Incluye la task completa
- Incluye el context pack base definido en `AGENT_CONTEXT_PACKS.md`
- Incluye documentos especificos por tipo de task
- Incluye documentos listados en Contexto Obligatorio
- Incluye ADRs y specs aplicables
- No ejecuta la task si falta un documento obligatorio

---

## Escenario 12 - Resource accounting de una ejecucion

**Given**
- Un agente ejecuta una task

**When**
- La ejecucion finaliza

**Then**
- Registra metricas de recursos si estan disponibles
- Registra modelo usado
- Registra tokens de entrada y salida si estan disponibles
- Registra tokens cacheados si estan disponibles
- Registra tool calls
- Registra MCP calls
- Registra context pack usado
- Registra presupuesto excedido si aplica
- Si las metricas no estan disponibles, reporta el motivo

---

## Quality Gates Relacionados

- Las tareas delegadas a agentes siguen `AGENT_TASK_TEMPLATE.md`
- Las tareas delegadas a agentes respetan `AGENT_BACKLOG_GOVERNANCE.md`
- Las tareas `Ready` se ejecutan segun `AGENT_TASK_EXECUTION.md`
- Las ejecuciones generan evidencia compatible con `AGENT_RUN_REPORT_TEMPLATE.md`
- El contexto de ejecucion se construye segun `AGENT_CONTEXT_PACKS.md`
- Las ejecuciones reportan resource accounting segun `AGENT_RESOURCE_ACCOUNTING.md` cuando sea posible
- No se ejecutan dependientes si una task requerida esta `Blocked` o `Needs Revision`
- Las referencias cruzadas del backlog son validas
- El orden numerico de agent tasks representa orden recomendado de ejecucion
