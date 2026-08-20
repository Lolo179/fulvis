# AGENT_ORCHESTRATION_PLAYBOOK.md - Fulvis

**Estado:** Fase 1  
**Ultima revision:** 2026-08-19

---

## Proposito

Este documento explica como aplicar trabajo agentic en Fulvis usando roles, tasks, context packs, quality gates, version control y revision.

Tambien aclara la diferencia entre:

```text
SDD
agentic development
harness
harness engineer
supervisor
executor agent
reviewer agent
```

---

## Aclaracion Principal

`Harness` no es exactamente el metodo completo de desarrollo.

En este proyecto usaremos esta distincion:

```text
SDD                  -> forma de dirigir desarrollo mediante specs como fuente de verdad
Agentic development  -> forma de ejecutar trabajo usando agentes con autonomia limitada
Harness              -> sistema de control que rodea a los agentes
Harness engineer     -> rol que disena, mantiene y mejora ese sistema de control
Supervisor           -> rol humano que decide, prioriza, acepta o bloquea
```

Una analogia util:

```text
SDD dice que construir.
Agentic development decide usar agentes para construirlo.
Harness define las vias, limites, sensores, frenos y reportes.
Harness engineer disena esas vias, limites, sensores, frenos y reportes.
Supervisor decide si el resultado entra o no.
```

---

## Que Es El Harness En Fulvis

El harness es el conjunto de procedimientos, documentos y herramientas que permiten que un agente trabaje sin salirse del carril.

Incluye:

```text
- agent tasks
- agent profiles
- agent briefs
- context packs
- quality gates
- specs
- ADRs
- version control
- worktrees
- validaciones
- run reports
- resource accounting
- reglas de escalacion
- tools, skills y MCPs en fases posteriores
```

El harness puede ser manual al principio.

Ejemplo en Fase 1:

```text
El supervisor lee una task Ready, entrega contexto a un agente, revisa diff, ejecuta checks y decide si aceptar.
```

Ejemplo en fases posteriores:

```text
Un sistema crea worktree, carga context pack, lanza agente, ejecuta gates, registra coste y abre PR.
```

---

## Roles

### Supervisor

Responsable de decidir.

Hace:

```text
- prioriza tasks
- aprueba que una task pase a Ready
- decide excepciones de orden
- acepta, bloquea o pide revision
- resuelve tensiones de negocio, arquitectura o contrato
```

No deberia depender de revisar cada linea generada.

Debe apoyarse en:

```text
- diffs
- specs
- quality gates
- tests
- run reports
- riesgos declarados
```

---

### Harness Engineer

Responsable de disenar el sistema de supervision.

Hace:

```text
- define el flujo de ejecucion
- define context packs
- define quality gates
- define limites de herramientas
- define politica de retries
- define reportes
- define medicion de recursos
- decide cuando introducir skills, tools o MCPs
```

En Fulvis, este es el rol que queremos practicar.

---

### Executor Agent

Responsable de ejecutar una task `Ready`.

Hace:

```text
- lee la task completa
- lee contexto obligatorio
- modifica solo archivos permitidos
- ejecuta validaciones
- reporta cambios, gaps y tensiones
- propone estado final
```

No decide cambios de negocio, arquitectura aceptada o contratos externos.

---

### Reviewer Agent

Responsable de revisar el resultado de otro agente.

Hace:

```text
- revisa diff
- compara contra task
- compara contra specs y ADRs
- revisa quality gates
- busca referencias rotas
- identifica riesgos de cascada
- propone Accepted, Blocked o Needs Revision
```

No deberia reimplementar la task mientras revisa.

---

## Flujo Basico Para Una Task Ready

```text
1. Supervisor elige una task Ready
2. Harness prepara branch o worktree
3. Harness construye context pack
4. Harness selecciona agent profile
5. Harness entrega la task y, si hace falta, un orchestration brief
6. Executor agent ejecuta la task
7. Executor agent genera salida compatible con run report
8. Harness ejecuta quality gates
9. Reviewer agent revisa diff, report y checks
10. Supervisor decide estado final
11. Si se acepta, se integra
12. Si falla, se clasifica sin continuar con dependientes
```

---

## Profiles Y Briefs

Un `agent profile` define comportamiento reutilizable de un rol.

Ejemplos:

```text
specs/agent-profiles/executor-agent.md
specs/agent-profiles/reviewer-agent.md
```

Un `.github/agents/*.agent.md` adapta ese rol a una interfaz ejecutable concreta.

Ejemplos:

```text
.github/agents/Executor.agent.md
.github/agents/Reviewer.agent.md
```

Regla:

```text
specs/agent-profiles/* es fuente de verdad del rol.
.github/agents/*.agent.md es adaptador ejecutable para la herramienta.
```

Un `orchestration brief` coordina una task cuando participan varios agentes, worktrees, revisiones separadas o un flujo especial.

No debe existir un brief por agente y por task por defecto.

La agent task debe ser la orden principal.

Un brief adicional solo se justifica si reduce ambiguedad real sin duplicar la task.

Ejemplo:

```text
specs/agent-briefs/001-orchestration-brief.md
```

Regla:

```text
El profile define el comportamiento estable del rol.
La task define el trabajo.
El orchestration brief coordina solo cuando hay mas de un agente o flujo especial.
```

---

## Modalidades De Ejecucion

### Modalidad A - Un Solo Agente

Uso recomendado:

```text
- cambios pequenos
- bajo riesgo
- documentacion simple
- correcciones mecanicas
```

Flujo:

```text
Supervisor -> Executor Agent -> checks -> Supervisor
```

Ventaja:

```text
rapida y barata.
```

Riesgo:

```text
menos separacion entre ejecucion y revision.
```

---

### Modalidad B - Executor + Reviewer

Uso recomendado:

```text
- primera ejecucion de una task importante
- cambios de estructura
- cambios donde queremos practicar supervision
- trabajo generado por IA que no queremos revisar linea por linea
```

Flujo:

```text
Supervisor -> Executor Agent -> checks -> Reviewer Agent -> Supervisor
```

Ventaja:

```text
separa construir de evaluar.
```

Riesgo:

```text
consume mas tiempo y tokens.
```

---

### Modalidad C - Varios Agentes En Worktrees

Uso recomendado:

```text
- experimentos
- decisiones tecnicas abiertas
- comparacion de enfoques
- aprendizaje de orquestacion
```

Flujo conceptual:

```text
worktrees/fulvis-agent-a-task-x
worktrees/fulvis-agent-b-task-x

Agente A propone solucion.
Agente B propone solucion.
Reviewer compara diffs contra specs y gates.
Supervisor elige, combina o descarta.
```

Ventaja:

```text
permite comparar soluciones reales.
```

Riesgo:

```text
puede ser excesivo si la decision no lo justifica.
```

Regla:

```text
No usar multiples agentes para demostrar sofisticacion. Usarlos cuando comparar alternativas aporte aprendizaje o reduzca riesgo.
```

---

## Interfaces De Ejecucion

Una interfaz de ejecucion puede ser cualquier entorno desde el que el supervisor lanza o coordina agentes.

En Fulvis, antes de usarla, deberiamos tener claro:

```text
- que task Ready se ejecuta
- que contexto debe leer el agente
- que archivos puede modificar
- que validaciones debe ejecutar
- que salida debe entregar
- que decision queda reservada al supervisor
```

La interfaz no sustituye al harness.

La interfaz solo es una forma de operar el harness.

Regla:

```text
Primero definimos el carril. Despues ponemos agentes a correr dentro del carril.
```

---

## Procedimiento Recomendado Para Fulvis

### Fase 1 - Practica Guiada

```text
- usar una task Ready
- ejecutar con un solo agente
- generar run report
- revisar manualmente contra specs y gates
```

Objetivo:

```text
aprender el ciclo completo sin demasiadas piezas moviendose a la vez.
```

### Fase 2 - Executor + Reviewer

```text
- un agente implementa
- otro agente revisa
- supervisor decide
```

Objetivo:

```text
separar construccion de evaluacion.
```

### Fase 3 - Worktrees

```text
- crear worktrees por agente
- comparar diffs
- medir coste y resultado
- aceptar una alternativa
```

Objetivo:

```text
aprender orquestacion multiagente sin mezclar cambios.
```

### Fase 4 - Tools, Skills y MCPs

```text
- extraer procedimientos repetidos como skills
- automatizar checks mediante tools
- conectar GitHub, CI o metricas mediante MCPs
```

Objetivo:

```text
pasar de supervision manual a harness parcialmente automatizado.
```

---

## Aplicacion A Task 001

Task:

```text
001-migrate-docs-to-specs-structure.md
```

Modalidad recomendada:

```text
Executor + Reviewer
```

Motivo:

```text
es una task documental y mecanica, pero afecta la estructura base del proyecto y muchas referencias cruzadas.
```

Procedimiento:

```text
1. Executor migra docs/ a specs/
2. Executor actualiza referencias internas
3. Executor valida ASCII y referencias a specs/
4. Reviewer revisa estructura, diff y referencias
5. Supervisor decide si aceptar
```

No recomendamos varios worktrees para esta task salvo que el objetivo principal sea practicar orquestacion.

Motivo:

```text
hay poca decision alternativa real; seria mas ceremonia que valor.
```

---

## Regla De Decision

Antes de ejecutar una task con agentes, elegir modalidad:

```text
Si es pequena y segura -> un solo agente
Si afecta estructura o queremos confianza -> executor + reviewer
Si hay alternativas tecnicas reales -> varios agentes en worktrees
```

El harness engineer no busca usar la opcion mas compleja.

Busca usar el nivel minimo de orquestacion que controle el riesgo y maximice aprendizaje.
