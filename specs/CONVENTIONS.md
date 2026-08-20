# CONVENTIONS.md - Fulvis

**Estado:** Fase 1  
**Ultima revision:** 2026-08-18

---

## Proposito

Este documento define convenciones de lenguaje, documentacion y organizacion para Fulvis.

Su objetivo es reducir ambiguedad entre humanos, agentes y futuras herramientas de generacion o revision.

---

## Idioma

- La documentacion explicativa se escribe en castellano.
- Los nombres tecnicos del sistema se escriben en ingles.
- Los ejemplos de API usan nombres tecnicos en ingles.
- Las ADRs pueden estar redactadas en castellano, pero deben respetar el vocabulario tecnico en ingles.

---

## Vocabulario Tecnico

### Agregados y entidades

```text
Order
OrderLine
OrderHistory
Item
```

### Estados

```text
MANAGED
IN_PREPARATION
SHIPPED
RECEIVED
CLOSED
CANCELLED
```

### Eventos

```text
ORDER_CREATED
START_PREPARATION
MARK_SHIPPED
CONFIRM_RECEPTION
CLOSE
CANCEL
```

### Acciones de stock

```text
NONE
RESERVE
RELEASE
DECREMENT
```

### Campos principales

En dominio, aplicacion y API se usan nombres en `camelCase`:

```text
orderId
itemId
status
lines
history
quantity
createdAt
updatedAt
occurredAt
previousStatus
newStatus
stockTotal
stockReserved
stockAvailable
reference
name
```

En persistencia relacional se permite usar `snake_case` para nombres de columnas:

```text
order_id
item_id
stock_total
stock_reserved
stock_available
created_at
updated_at
```

---

## Organizacion Documental

La estructura actual vive bajo `specs/`.

Estos documentos no se tratan como documentacion pasiva, sino como fuentes de verdad, specs, quality gates y tareas operativas para agentes.

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

- `REQUIREMENTS.md`: requisitos funcionales, reglas de negocio y restricciones.
- `DOMAIN.md`: modelo de dominio, agregados, invariantes y responsabilidades.
- `STATE_MACHINE.md`: estados, eventos y transiciones del ciclo de vida de `Order`.
- `APPLICATION.md`: casos de uso, orquestacion, puertos y errores conceptuales.
- `API.md`: contrato REST conceptual y mapeo hacia comandos/resultados de aplicacion.
- `QUALITY_GATES.md`: puertas de calidad para validar implementaciones humanas o generadas por IA.
- `specs/use-cases/*`: specs de comportamiento de casos de uso en formato Given-When-Then.
- `specs/architecture/*`: specs de arquitectura, estructura, persistencia, observabilidad y governance.
- `SPEC_COVERAGE.md`: matriz de trazabilidad entre requisitos, decisiones, specs y quality gates.
- `AGENT_TASK_TEMPLATE.md`: plantilla para definir tareas de agentes con contexto, alcance y criterios de done.
- `AGENT_BACKLOG_GOVERNANCE.md`: reglas de estado, fallo, reintento y actualizacion de agent tasks.
- `AGENT_READINESS_REVIEW.md`: gate recurrente para promover agent tasks de `Draft` a `Ready`.
- `AGENT_TASK_EXECUTION.md`: protocolo para ejecutar una task `Ready` con controles de alcance, validacion y reintento.
- `AGENT_RUN_REPORT_TEMPLATE.md`: plantilla de evidencia para registrar una ejecucion de agente.
- `AGENT_CONTEXT_PACKS.md`: reglas para construir el contexto minimo suficiente de cada ejecucion.
- `AGENT_TOOLCHAIN.md`: mapa de herramientas, capacidades, riesgos y fases para supervision agentic.
- `AGENT_ORCHESTRATION_PLAYBOOK.md`: guia de roles y modalidades para ejecutar trabajo agentic.
- `AGENT_RESOURCE_ACCOUNTING.md`: politica para medir tokens, costes, tools, MCPs y presupuestos de ejecucion.
- `specs/agent-profiles/*`: perfiles reutilizables de agentes por rol.
- `specs/agent-briefs/*`: briefs de orquestacion para tasks que requieren coordinacion multiagente o flujo especial.
- `.github/agents/*`: adaptadores ejecutables para cargar perfiles de agentes en herramientas concretas.
- `specs/agent-tasks/*`: tareas concretas delegables a agentes.
- `specs/adr/*`: decisiones arquitectonicas con contexto, alternativas, decision y consecuencias.

### Orden de agent tasks

El prefijo numerico de `specs/agent-tasks/*` representa el orden recomendado de ejecucion, no el orden en que la tarea fue descubierta durante el analisis.

Si se detecta una tarea que debe ejecutarse antes que otras ya existentes, el backlog debe normalizarse:

- renombrar archivos
- ajustar encabezados internos
- actualizar referencias cruzadas
- actualizar `SPEC_COVERAGE.md` si aplica

Una task puede depender de otras tasks anteriores, pero no debe depender implicitamente de una task numericamente posterior salvo que se marque como tension o excepcion explicita.

---

## Jerarquia de Fuentes de Verdad

Cada documento es fuente de verdad de una parte distinta del sistema.

```text
REQUIREMENTS.md  -> necesidades de negocio, reglas y restricciones generales
DOMAIN.md        -> conceptos del dominio, agregados, invariantes y responsabilidades
STATE_MACHINE.md -> estados, eventos y transiciones validas de Order
ADR-*            -> decisiones arquitectonicas, alternativas descartadas y consecuencias
APPLICATION.md   -> casos de uso, orquestacion, contratos y errores conceptuales
API.md           -> contrato REST conceptual y mapeo HTTP
specs/use-cases/* -> comportamiento verificable por escenario de caso de uso
specs/architecture/* -> comportamiento verificable de arquitectura y governance
QUALITY_GATES.md -> restricciones globales de aceptacion
SPEC_COVERAGE.md -> trazabilidad de cobertura entre fuentes y verificaciones
AGENT_TASK_TEMPLATE.md -> formato operativo para delegar trabajo a agentes
AGENT_BACKLOG_GOVERNANCE.md -> reglas para ejecutar, corregir y recuperar tasks
AGENT_READINESS_REVIEW.md -> gate recurrente para preparar la siguiente task ejecutable
AGENT_TASK_EXECUTION.md -> protocolo operativo para ejecutar una task Ready
AGENT_RUN_REPORT_TEMPLATE.md -> evidencia estructurada de ejecuciones de agentes
AGENT_CONTEXT_PACKS.md -> composicion de contexto para agentes, tools y harnesses
AGENT_TOOLCHAIN.md -> capacidades, herramientas y riesgos del harness de supervision
AGENT_ORCHESTRATION_PLAYBOOK.md -> roles y modalidades de orquestacion agentic
AGENT_RESOURCE_ACCOUNTING.md -> contabilidad de recursos, tokens, costes, tools y MCPs
specs/agent-profiles/* -> comportamiento reutilizable de agentes por rol
specs/agent-briefs/* -> coordinacion multiagente o flujo especial por task cuando se justifique
.github/agents/* -> adaptadores ejecutables de agentes para herramientas concretas
specs/agent-tasks/* -> ordenes de trabajo concretas para agentes
```

Las specs son fuente de verdad del comportamiento esperado, pero no pueden contradecir requisitos, modelo de dominio, state machine o ADRs aceptadas.

Si una spec entra en tension con otro documento de mayor alcance, no se implementa automaticamente. Primero se marca la tension y se decide si debe cambiar la spec, el requisito, el modelo o una ADR.

---

## Reglas de Diseno

- El dominio no conoce HTTP, bases de datos ni frameworks.
- Los adaptadores externos no toman decisiones de negocio.
- Los casos de uso coordinan agregados, pero no absorben invariantes internas.
- Las invariantes de `Order` viven en `Order`.
- Las invariantes de stock viven en `Item`.
- La politica de transiciones vive en `OrderStateMachine`.
- Las transiciones se solicitan mediante `OrderEvent`, no mediante estado destino.
- No se introducen abstracciones nuevas sin una necesidad clara.

---

## Control de Versiones

- Fulvis usara Git como sistema de control de versiones.
- El remoto principal vivira en GitHub.
- `main` representa el estado aceptado del proyecto.
- Cambios no triviales deben realizarse en ramas separadas.
- Pull Requests funcionaran como frontera de revision y supervision.
- Git worktrees podran usarse para experimentos con multiples agentes o enfoques alternativos.
- Los resultados de agentes se evaluaran mediante diffs, specs, quality gates y checks.
- Las tareas delegadas a agentes deben seguir `AGENT_TASK_TEMPLATE.md` cuando no sean triviales.
- Las tareas delegadas a agentes deben respetar `AGENT_BACKLOG_GOVERNANCE.md`.
- Las tareas en estado `Ready` deben ejecutarse siguiendo `AGENT_TASK_EXECUTION.md`.
- Las ejecuciones de agentes deben poder reportarse con `AGENT_RUN_REPORT_TEMPLATE.md`.
- El contexto entregado a agentes debe construirse segun `AGENT_CONTEXT_PACKS.md`.
- Las herramientas usadas por agentes deben respetar `AGENT_TOOLCHAIN.md`.
- La orquestacion de agentes debe seguir `AGENT_ORCHESTRATION_PLAYBOOK.md`.
- Las ejecuciones deben registrar metricas de recursos segun `AGENT_RESOURCE_ACCOUNTING.md` cuando sea posible.

Ver `specs/architecture/version-control.spec.md`.

---

## Convenciones Para Trabajo Con IA

Cuando un agente genere, revise o modifique diseno o codigo para Fulvis, debe:

- leer primero `REQUIREMENTS.md`, `DOMAIN.md`, `APPLICATION.md`, `STATE_MACHINE.md`, `API.md`, `QUALITY_GATES.md`, `SPEC_COVERAGE.md`, las specs relevantes y las ADRs relevantes
- respetar `AGENT_BACKLOG_GOVERNANCE.md` cuando ejecute o modifique agent tasks
- respetar `AGENT_TASK_EXECUTION.md` cuando ejecute una task `Ready`
- entregar una salida compatible con `AGENT_RUN_REPORT_TEMPLATE.md`
- usar un contexto compatible con `AGENT_CONTEXT_PACKS.md`
- respetar el mapa de herramientas y puntos de supervision de `AGENT_TOOLCHAIN.md`
- respetar los roles y modalidades definidos en `AGENT_ORCHESTRATION_PLAYBOOK.md`
- reportar metricas de recursos segun `AGENT_RESOURCE_ACCOUNTING.md` cuando esten disponibles
- respetar el vocabulario tecnico definido en este documento
- no inventar nuevos estados, eventos o errores sin actualizar los documentos correspondientes
- justificar decisiones arquitectonicas nuevas mediante ADR
- mantener separadas reglas de negocio, decisiones de arquitectura y detalles de infraestructura
- senalar cualquier tension entre documentos antes de implementar

---

## Criterio de Cambio

Si una convencion deja de encajar, se puede cambiar, pero el cambio debe ser explicito.

Cambios que afecten a vocabulario, limites de responsabilidad, consistencia, concurrencia, idempotencia o contratos externos deben documentarse mediante ADR o actualizacion del documento correspondiente.
