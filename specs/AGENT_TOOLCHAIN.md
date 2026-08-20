# AGENT_TOOLCHAIN.md - Fulvis

**Estado:** Fase 1  
**Ultima revision:** 2026-08-19

---

## Proposito

Este documento define el toolchain minimo que necesita un supervisor de agentes o harness engineer para dirigir trabajo agentic de forma controlada.

No define contenido reutilizable de proyecto.

Define procedimiento y capacidades:

```text
que herramientas necesita el harness, para que sirven, que riesgo reducen y cuando requieren supervision humana.
```

---

## Principio

Un agente no deberia recibir una orden y operar "a ciegas".

Debe operar dentro de un entorno con:

```text
contexto controlado
permisos limitados
validaciones ejecutables
trazabilidad de cambios
metricas de consumo
protocolo de fallo
```

La productividad con IA no viene de revisar cada linea manualmente.

Viene de rodear la ejecucion con limites, tests, specs, quality gates, version control y evidencias.

---

## Capacidades Minimas

### 1. Acceso a contexto

El harness debe poder leer:

```text
- tasks
- specs
- ADRs
- quality gates
- convenciones
- documentos de dominio, aplicacion y API
- reportes previos
```

Riesgo que controla:

```text
evita que el agente invente reglas por falta de contexto.
```

Documento relacionado:

```text
AGENT_CONTEXT_PACKS.md
```

---

### 2. Busqueda e indexacion

El harness debe poder buscar referencias en el repositorio:

```text
- nombres de specs
- IDs de requisitos
- ADRs citadas
- endpoints
- eventos
- estados
- quality gates
```

Herramientas tipicas:

```text
rg
repo search
document index
semantic search opcional
```

Riesgo que controla:

```text
evita duplicar decisiones, ignorar documentos existentes o introducir contradicciones.
```

---

### 3. Escritura controlada de archivos

El agente debe poder modificar archivos, pero solo dentro del alcance autorizado por la task.

El harness debe distinguir:

```text
- archivos permitidos
- archivos fuera de alcance
- cambios mecanicos
- cambios que requieren supervisor
```

Riesgo que controla:

```text
evita que un fallo local se convierta en cambio arquitectonico accidental.
```

Documento relacionado:

```text
AGENT_TASK_EXECUTION.md
```

---

### 4. Version control

Git y GitHub deben actuar como frontera de seguridad.

Capacidades esperadas:

```text
- rama por cambio no trivial
- diff visible
- commit base identificable
- pull request para revision
- checks asociados al PR
- worktrees para ejecuciones paralelas
```

Riesgo que controla:

```text
evita perdida de trazabilidad, mezcla de trabajos y cascadas entre agentes.
```

Documento relacionado:

```text
specs/architecture/version-control.spec.md
```

---

### 5. Ejecucion de validaciones

El harness debe poder ejecutar las validaciones definidas por cada task.

Ejemplos:

```text
mvn test
mvn verify
unit tests
integration tests
architecture checks
OpenAPI validation
ASCII check para docs
referencias cruzadas
```

Riesgo que controla:

```text
reduce la necesidad de revisar manualmente codigo generado y convierte specs en comprobaciones.
```

Documento relacionado:

```text
QUALITY_GATES.md
```

---

### 6. Integracion con CI

El CI debe repetir los quality gates importantes fuera del entorno local del agente.

Capacidades esperadas:

```text
- build limpio
- tests unitarios
- tests de integracion cuando existan
- checks de arquitectura
- validacion OpenAPI
- reporte de fallos
- bloqueo de merge si fallan gates criticos
```

Riesgo que controla:

```text
evita aceptar cambios que solo funcionaron en el entorno local de un agente.
```

---

### 7. MCPs y conectores externos

Los MCPs conectan el harness con herramientas externas.

Posibles integraciones:

```text
GitHub        -> PRs, checks, issues, branches
issue tracker -> backlog, estado de tasks, asignacion
observability -> logs, trazas, metricas
database      -> inspeccion controlada de datos no sensibles
documentation wiki     -> consulta de fuentes corporativas
cost APIs     -> uso, coste y presupuestos
```

Riesgo que controlan:

```text
evitan trabajo manual repetitivo, pero introducen riesgo de permisos, datos sensibles y efectos externos.
```

Regla:

```text
Todo MCP con capacidad de escritura o efectos externos debe tener permisos minimos y puntos claros de aprobacion.
```

---

### 8. Resource accounting

El harness debe medir el coste de ejecucion.

Metricas minimas:

```text
tokens de entrada
tokens de salida
tokens cacheados
tool calls
MCP calls
reintentos
coste estimado
context pack usado
```

Riesgo que controla:

```text
evita loops caros, contexto excesivo y decisiones de orquestacion sin datos.
```

Documento relacionado:

```text
AGENT_RESOURCE_ACCOUNTING.md
```

---

### 9. Reporte de ejecucion

Cada ejecucion debe producir evidencia.

Debe responder:

```text
que hizo
por que lo hizo
que contexto leyo
que cambio
que valido
que fallo
que reintento
que coste tuvo
que decision final propone
```

Riesgo que controla:

```text
evita que una ejecucion de agente sea una caja negra.
```

Documento relacionado:

```text
AGENT_RUN_REPORT_TEMPLATE.md
```

---

### 10. Escalacion humana

El harness debe saber cuando parar.

Requieren supervisor:

```text
- cambiar negocio
- cambiar arquitectura aceptada
- modificar ADRs
- cambiar contratos API
- relajar quality gates
- introducir herramientas externas nuevas
- acceder a datos sensibles
- superar presupuesto relevante
- tercer intento tras dos fallos
```

Riesgo que controla:

```text
evita automatizar decisiones que no son puramente tecnicas.
```

---

## Fases de Madurez

### Fase 1 - Local

```text
- documentos Markdown
- tasks en repo
- ejecucion manual asistida
- busqueda con rg
- validaciones locales
- run report manual
- metricas estimadas
```

Objetivo:

```text
aprender el procedimiento sin depender todavia de herramientas externas.
```

### Fase 2 - GitHub y CI

```text
- ramas
- pull requests
- checks automaticos
- reportes asociados a PR
- proteccion de main
```

Objetivo:

```text
convertir supervision en flujo repetible.
```

### Fase 3 - MCPs y metricas

```text
- GitHub MCP
- issue tracker MCP
- observability MCP
- almacenamiento de AgentRun
- dashboard de coste y eficacia
```

Objetivo:

```text
conectar el harness con herramientas reales sin perder control.
```

### Fase 4 - Orquestacion

```text
- seleccion automatica de context pack
- ejecucion de agentes especializados
- reintentos controlados
- comparacion entre ramas o worktrees
- propuestas automaticas de Blocked, Needs Revision o Accepted
```

Objetivo:

```text
pasar de asistencia individual a supervision de sistema.
```

---

## Mapa de Riesgos

| Riesgo | Herramienta o control |
|---|---|
| Contexto insuficiente | `AGENT_CONTEXT_PACKS.md`, busqueda, specs |
| Decision inventada | ADRs, hierarchy of truth, supervisor gates |
| Cambio fuera de alcance | task scope, diff, Git |
| Bug no detectado | tests, CI, quality gates |
| Inconsistencia arquitectonica | architecture checks, ADR review |
| Loop caro | maxRetries, resource accounting, budget |
| Tool insegura | permisos minimos, approval points |
| Dato sensible expuesto | redaccion de reportes, secrets policy |
| Cascada entre tasks | backlog governance, readiness review |
| Caja negra | run report, diffs, metrics |

---

## Regla Para Fulvis

Fulvis no introducira una herramienta externa solo por moda.

Antes de agregar un MCP, plugin, dashboard o automatizacion, debe quedar claro:

```text
que decision mejora
que riesgo reduce
que coste introduce
que permisos necesita
como se audita
como se desactiva si falla
```

---

## Decision Actual Sobre Skills y Tools

En Fase 1, Fulvis no requiere skills ni tools propias.

Motivo:

```text
todavia estamos definiendo arquitectura, specs, quality gates, tasks y protocolo de supervision.
```

Estas actividades pueden realizarse con capacidades basicas:

```text
- lectura de documentos
- edicion controlada
- busqueda en repositorio
- validacion de consistencia
- ejecucion manual de quality gates cuando existan
```

No obstante, skills, tools y MCPs forman parte del aprendizaje previsto del proyecto.

Se introduciran en fases posteriores cuando exista al menos una de estas necesidades:

```text
- repetir muchas veces el mismo procedimiento
- encapsular instrucciones reutilizables para agentes
- ejecutar validaciones de forma automatica
- conectar con herramientas externas
- medir coste, tokens o resultados de forma sistematica
- orquestar varios agentes o worktrees
```

Ejemplos futuros:

```text
Skill: revisar si una agent task puede pasar de Draft a Ready
Skill: generar o revisar specs Given-When-Then
Skill: clasificar fallos de ejecucion como Blocked o Needs Revision
Tool: ejecutar quality gates
Tool: validar contrato OpenAPI
Tool: calcular cobertura de specs
MCP: conectar con GitHub para PRs, checks e issues
MCP: conectar con almacenamiento de AgentRun y metricas
```

Regla:

```text
Primero repeticion o integracion real. Despues skill, tool o MCP.
```

---

## Nota Para Harness

Este documento puede transformarse en una checklist previa a cada ejecucion:

```text
1. Existe task Ready?
2. Existe context pack suficiente?
3. Existen herramientas para leer y modificar solo lo permitido?
4. Existen validaciones ejecutables?
5. Existe rama o worktree controlado?
6. Existe protocolo de reporte?
7. Existe presupuesto?
8. Existen limites de reintento?
9. Estan claros los puntos de aprobacion humana?
10. Puede recuperarse el sistema si la ejecucion falla?
```
