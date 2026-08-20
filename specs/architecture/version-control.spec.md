# Spec - Version Control

**Estado:** Fase 1  
**Ultima revision:** 2026-08-18

---

## Proposito

Definir las reglas de control de versiones para Fulvis.

Esta spec prepara el proyecto para trabajo con Git, GitHub, PRs, checks, worktrees y experimentacion con multiples agentes.

---

## Rule 1 - Git and GitHub

**Given**
- Existe una implementacion o documentacion de Fulvis

**When**
- Se gestiona su historial

**Then**
- El proyecto debe usar Git
- El remoto principal debe estar en GitHub
- El historial debe permitir auditar cambios en requisitos, ADRs, specs, quality gates y codigo
- Los cambios relevantes deben quedar en commits trazables

---

## Rule 2 - Main branch as accepted state

**Given**
- Existe una rama principal del repositorio

**When**
- Se consulta el estado aceptado del proyecto

**Then**
- `main` representa el estado aceptado
- `main` no debe contener trabajo experimental incompleto
- Cambios en `main` deben haber pasado las quality gates disponibles
- Cambios en `main` no deben contradecir ADRs aceptadas ni specs vigentes

---

## Rule 3 - Branches for changes

**Given**
- Se va a introducir un cambio no trivial

**When**
- Se inicia el trabajo

**Then**
- Debe hacerse en una rama separada
- La rama debe tener un nombre descriptivo
- La rama debe enfocarse en una unidad de cambio coherente
- No debe mezclar cambios de dominio, API, infraestructura y documentacion sin justificacion

Ejemplos de nombres:

```text
specs/add-version-control-spec
feature/create-order-use-case
feature/order-state-machine
test/persistence-concurrency
experiment/agent-order-domain
```

---

## Rule 4 - Pull requests as review boundary

**Given**
- Existe un cambio candidato a entrar en `main`

**When**
- Se abre un Pull Request

**Then**
- El PR debe describir el objetivo del cambio
- El PR debe indicar specs, ADRs o requisitos afectados
- El PR debe indicar quality gates relevantes
- El PR debe listar gaps conocidos si existen
- El PR no debe esconder decisiones arquitectonicas nuevas

---

## Rule 5 - Worktrees for multi-agent experiments

**Given**
- Se experimenta con multiples agentes o enfoques alternativos

**When**
- Se crean lineas de trabajo paralelas

**Then**
- Cada agente o experimento debe trabajar en una rama separada
- Se puede usar Git worktree para aislar carpetas de trabajo
- Cada worktree debe tener un objetivo claro
- Los resultados de agentes distintos deben compararse por specs, quality gates y diffs
- No se debe mezclar salida de varios agentes sin integracion explicita

Ejemplo conceptual:

```text
worktrees/
  fulvis-agent-a-order-domain
  fulvis-agent-b-application-layer
  fulvis-agent-c-persistence
```

---

## Rule 6 - Commit discipline

**Given**
- Se confirma un cambio en Git

**When**
- Se escribe el commit

**Then**
- El commit debe representar una unidad coherente
- El mensaje debe explicar el cambio de forma breve
- Si afecta arquitectura, debe mencionar la ADR o documento afectado
- Si afecta comportamiento, debe mencionar la spec o requisito afectado
- No se deben mezclar cambios mecanicos con decisiones conceptuales sin aclararlo

---

## Rule 7 - Agent supervision through Git artifacts

**Given**
- Un agente genera o modifica trabajo

**When**
- El supervisor evalua el resultado

**Then**
- La evaluacion debe apoyarse en diffs, specs, quality gates y checks
- El supervisor no debe depender solo de lectura linea por linea
- El agente debe indicar que documentos uso como contexto
- El agente debe marcar tensiones entre documentos si las detecta
- El agente no debe modificar ADRs aceptadas sin decision explicita

---

## Quality Gates Relacionados

- Cambios trazables en Git
- GitHub como remoto principal
- `main` como estado aceptado
- PRs como frontera de revision
- Worktrees para experimentos multiagente
- Evaluacion por specs, quality gates y diffs
