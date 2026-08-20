# Agent Task 017 - Configure CI and PR Workflow

**Estado:** Draft  
**Ultima revision:** 2026-08-19

---

## Task

```text
Nombre: Configure CI and PR Workflow
Objetivo: Configurar el flujo de GitHub Actions, Pull Requests y supervision para que cada cambio pase por quality gates antes de entrar en main.
Tipo: testing
Rama sugerida: chore/ci-pr-workflow
Worktree sugerido: worktrees/fulvis-ci-pr-workflow
```

---

## Contexto Obligatorio

El agente debe leer antes de trabajar:

```text
specs/CONVENTIONS.md
specs/QUALITY_GATES.md
specs/SPEC_COVERAGE.md
specs/architecture/version-control.spec.md
specs/architecture/project-structure.spec.md
specs/architecture/architecture.spec.md
specs/agent-tasks/002-create-project-scaffold.md
specs/agent-tasks/015-implement-executable-quality-gates.md
specs/agent-tasks/016-implement-resilience-integration-tests.md
```

---

## Fuentes de Verdad Aplicables

```text
Version control:
- Git y GitHub
- main como estado aceptado
- ramas para cambios no triviales
- PRs como frontera de revision
- worktrees para experimentos multiagente
- commits trazables
- evaluacion por diffs, specs, quality gates y checks

Quality gates:
- specs/QUALITY_GATES.md
- checks ejecutables de Agent Task 015
- resilience integration tests from Agent Task 016

Project structure:
- Maven
- Java 21
- mvn test
- mvn verify
```

---

## Alcance

### Puede modificar

```text
- .github/workflows/**
- .github/pull_request_template.md
- README.md
- scripts/**
- pom.xml solo si necesita ajustar comandos verificables
```

### No puede modificar

```text
- specs/** salvo para actualizar SPEC_COVERAGE.md si cambia cobertura
- src/main/.../domain/**
- src/main/.../application/**
- src/main/.../adapters/**
- contratos OpenAPI salvo tension documentada
```

### Fuera de alcance

```text
- Implementar funcionalidades
- Cambiar reglas de negocio
- Cambiar arquitectura aceptada
- Configurar secretos reales
- Publicar artefactos externos
- Crear ramas o PRs reales sin instruccion explicita del supervisor
```

---

## Restricciones

```text
- Respetar vocabulario tecnico en ingles definido en CONVENTIONS.md
- No modificar ADRs aceptadas sin decision explicita
- CI debe ejecutar comandos reproducibles localmente
- CI no debe depender de servicios externos manuales
- CI no debe requerir secretos para checks basicos
- main debe representar estado aceptado
- PR template debe pedir specs, ADRs, quality gates y gaps
- PR template debe pedir validaciones ejecutadas
- No ocultar gaps conocidos
- Marcar tensiones entre documentos antes de implementar
```

---

## Workflow Esperado

Workflow conceptual:

```text
on:
- pull_request
- push to main

jobs:
- verify
```

El job `verify` debe:

```text
- usar Java 21
- cachear dependencias Maven si aplica
- ejecutar mvn verify
- publicar resultado claro si falla
```

Si existen perfiles separados:

```text
- ejecutar unit tests
- ejecutar architecture checks
- ejecutar OpenAPI validation
- ejecutar resilience integration tests con Testcontainers cuando esten disponibles
```

---

## Pull Request Template Esperado

El template de PR debe pedir:

```text
Resumen:

Specs afectadas:

ADRs afectadas:

Quality gates relevantes:

Agent task relacionada:

Validaciones ejecutadas:

Gaps conocidos:

Tensiones detectadas:
```

---

## Reglas Para Trabajo Multiagente

```text
- Cada agente trabaja en una rama separada
- Cada experimento puede usar un worktree separado
- Las salidas se comparan por diff, specs y quality gates
- No se mezclan salidas de agentes sin integracion explicita
- El PR representa la frontera de aceptacion hacia main
```

---

## Specs que Debe Satisfacer

```text
- specs/architecture/version-control.spec.md
- specs/architecture/project-structure.spec.md
- specs/architecture/architecture.spec.md
```

---

## Quality Gates Aplicables

```text
- Version control and agent workflow
- Stack obligatorio
- Arquitectura hexagonal
- API-First
- Unit tests
- Integration tests cuando existan
- Resilience integration tests
- Executable quality gates
```

---

## Criterio de Done

La tarea se considera terminada cuando:

```text
- Existe workflow de GitHub Actions para PRs
- Existe workflow de GitHub Actions para push a main
- El workflow usa Java 21
- El workflow ejecuta mvn verify o comando equivalente documentado
- El workflow ejecuta pruebas de resiliencia si ya forman parte de mvn verify
- El workflow no requiere secretos para checks basicos
- Existe pull_request_template.md
- El template pide specs afectadas
- El template pide ADRs afectadas
- El template pide quality gates relevantes
- El template pide agent task relacionada
- El template pide validaciones ejecutadas
- El template pide gaps y tensiones
- README.md documenta comandos locales de verificacion si aplica
- SPEC_COVERAGE.md se actualiza si cambia cobertura
- El agente reporta gaps, riesgos o tensiones detectadas
```

---

## Output Esperado del Agente

El agente debe entregar:

```text
Resumen del cambio:

Docs leidos:

Specs satisfechas:

Quality gates relevantes:

Archivos modificados:

Tensiones detectadas:

Gaps restantes:

Validaciones ejecutadas:
```

---

## Politica de Revision

El supervisor evaluara la tarea mediante:

```text
- diff
- contenido de .github/workflows/**
- contenido de .github/pull_request_template.md
- ejecucion local de mvn verify
- specs/architecture/version-control.spec.md
- specs/QUALITY_GATES.md
- claridad de los checks en PR
```

La revision humana debe centrarse en que GitHub sea una frontera de supervision, no solo un almacen remoto de codigo.
