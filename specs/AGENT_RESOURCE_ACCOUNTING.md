# AGENT_RESOURCE_ACCOUNTING.md - Fulvis

**Estado:** Fase 1  
**Ultima revision:** 2026-08-19

---

## Proposito

Este documento define como medir y controlar el consumo de recursos durante ejecuciones de agentes.

El objetivo es aprender y practicar supervision de costes, tokens, tools, MCPs y contexto sin mezclar esta preocupacion con el dominio de Fulvis.

---

## Principio

Optimizar recursos no significa dar menos contexto a ciegas.

Significa:

```text
dar el contexto justo, con la herramienta correcta, en el momento correcto, y registrar cuanto costo.
```

Una ejecucion de agente debe poder responder:

```text
- cuanto contexto se cargo
- cuantos tokens se usaron
- cuantos tokens fueron cacheados
- cuantas tools se llamaron
- cuantos MCP calls se hicieron
- cuanto costo estimado tuvo la ejecucion
- que parte del coste vino de reintentos
- que parte del coste vino de contexto innecesario
```

---

## Unidad de Medicion

La unidad principal es:

```text
AgentRun
```

Un `AgentRun` corresponde a una ejecucion de una agent task.

Relacion conceptual:

```text
AgentRun
  -> task
  -> context pack
  -> toolchain
  -> model calls
  -> tool calls
  -> mcp calls
  -> validations
  -> run report
```

---

## Metricas Minimas

Cada ejecucion deberia registrar:

```text
runId
taskId
taskType
agentName
model
startedAt
finishedAt
status
inputTokens
outputTokens
cachedInputTokens
totalTokens
estimatedCost
contextPackName
documentsLoaded
documentCount
toolchainPhase
toolCallCount
mcpCallCount
retryCount
validationCount
blockedReason
needsRevisionReason
```

---

## Metricas Por Tool o MCP

Cada llamada a tool o MCP deberia registrar:

```text
runId
toolName
mcpServerName
operation
startedAt
finishedAt
success
inputSummary
outputSummary
estimatedTokensIn
estimatedTokensOut
errorType
```

Notas:

```text
- inputSummary y outputSummary no deben almacenar secretos.
- Si la tool devuelve datos grandes, registrar resumen, tamano y referencia estable.
- No guardar payloads sensibles salvo decision explicita.
```

---

## Presupuesto Por Ejecucion

Cada task puede definir un presupuesto orientativo:

```text
maxInputTokens
maxOutputTokens
maxTotalTokens
maxToolCalls
maxMcpCalls
maxRetries
```

Si una ejecucion supera o parece que va a superar presupuesto:

```text
- detenerse si el riesgo es alto
- resumir estado actual
- reportar decision requerida
- proponer reducir contexto o dividir task
```

---

## Politica de Contexto

`AGENT_CONTEXT_PACKS.md` define que contexto debe cargarse.

`AGENT_TOOLCHAIN.md` define que herramientas y capacidades puede usar el harness.

Resource accounting mide:

```text
- que documentos se cargaron realmente
- cuantos eran obligatorios
- cuantos eran opcionales
- cuantos resultaron innecesarios
- si se cargo contexto duplicado
- si se cargo contexto demasiado grande
- que herramientas se usaron realmente
- que herramientas esperadas no estaban disponibles
```

Regla:

```text
No omitir fuentes de verdad criticas solo para ahorrar tokens.
```

---

## Estrategia Por Fases

### Fase 1 - Local y simple

```text
- Registrar metricas en run report
- Guardar metricas opcionalmente en JSON o CSV local
- Estimar tokens de documentos cargados cuando no exista usage real
- Contar tool calls y MCP calls
```

### Fase 2 - Almacenamiento consultable

```text
- Guardar AgentRun en SQLite o PostgreSQL
- Consultar coste por task, agente, tipo de task y modelo
- Comparar context packs
- Detectar retries caros
```

### Fase 3 - Integracion externa

```text
- Consultar OpenAI Usage API
- Consultar OpenAI Costs API
- Correlacionar por project_id, api_key_id, user_id, model o metadata disponible
- Crear dashboard
- Definir alertas de presupuesto
```

---

## Fuentes Externas Posibles

OpenAI ofrece endpoints de uso y costes a nivel organizacion.

Uso conceptual:

```text
Usage API -> consumo agregado de tokens y llamadas
Costs API -> costes agregados y reconciliables con facturacion
```

Estas fuentes sirven para contabilidad agregada.

El harness local sigue siendo necesario para saber:

```text
- que task genero el coste
- que agente lo genero
- que context pack se uso
- que MCPs se llamaron
- que reintentos ocurrieron
```

---

## Preguntas Que Debe Poder Responder

```text
Cuanto cuesta ejecutar una task?
Que tipo de task consume mas tokens?
Que agente consume mas recursos?
Que context pack es demasiado grande?
Cuanto cuestan los reintentos?
Que MCP genera mas llamadas?
Que porcentaje de tokens fue cacheado?
Que ejecuciones terminaron en Blocked tras consumir recursos?
Que tasks deberian dividirse por coste?
```

---

## Run Report

`AGENT_RUN_REPORT_TEMPLATE.md` debe incluir una seccion de resource accounting cuando existan datos.

Formato conceptual:

```text
Resource accounting:

Model:
Input tokens:
Output tokens:
Cached input tokens:
Total tokens:
Estimated cost:
Tool calls:
MCP calls:
Context pack:
Toolchain phase:
Documents loaded:
Retry cost:
Budget exceeded: yes/no
```

---

## Decisiones Que Requieren Supervisor

El agente no debe decidir automaticamente:

```text
- saltarse contexto obligatorio por coste
- cambiar modelo solo por coste
- desactivar quality gates para ahorrar tokens
- ocultar tool calls fallidas
- borrar metricas de ejecucion
- enviar metricas sensibles a herramientas externas
```

---

## Nota Para Harness

Un harness puede implementar este flujo:

```text
1. Crear runId
2. Registrar taskId y metadata
3. Construir context pack
4. Estimar coste previo si es posible
5. Ejecutar agente
6. Capturar usage de cada model call
7. Capturar tool calls y MCP calls
8. Registrar validaciones
9. Calcular coste estimado
10. Escribir run report
11. Persistir metricas si existe storage
```
