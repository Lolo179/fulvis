# SPEC_COVERAGE.md - Fulvis

**Estado:** Fase 1  
**Ultima revision:** 2026-08-18

---

## Proposito

Este documento traza requisitos, decisiones arquitectonicas y restricciones hacia specs o quality gates.

No busca crear burocracia. Busca responder una pregunta de supervision:

```text
Como sabemos que esta regla o decision queda cubierta?
```

Estados:

```text
Covered -> existe una spec, quality gate o contrato que lo verifica de forma clara
Partial -> existe cobertura parcial, pero falta precision o escenario adicional
Gap     -> aun no existe una forma clara de verificarlo
```

---

## Requisitos Funcionales

| Fuente | Item | Verificacion | Estado |
|---|---|---|---|
| `REQ-S01` | Consultar stock | `get-item-stock.spec.md`, `API.md` - `GET /items/{itemId}/stock` | Covered |
| `REQ-S02` | Reservar stock al crear pedido | `create-order.spec.md` - Escenarios 1 y 2 | Covered |
| `REQ-S03` | Liberar stock al cancelar pedido | `transition-order.spec.md` - Escenarios 5 y 6 | Covered |
| `REQ-S04` | Decrementar stock al marcar enviado | `transition-order.spec.md` - Escenario 2 | Covered |
| `REQ-P01` | Crear pedido en `MANAGED` | `create-order.spec.md` - Escenario 1 | Covered |
| `REQ-P02` | `MANAGED -> IN_PREPARATION` | `transition-order.spec.md` - Escenario 1 | Covered |
| `REQ-P03` | `IN_PREPARATION -> SHIPPED` | `transition-order.spec.md` - Escenario 2 | Covered |
| `REQ-P04` | `SHIPPED -> RECEIVED` | `transition-order.spec.md` - Escenario 3 | Covered |
| `REQ-P05` | `RECEIVED -> CLOSED` | `transition-order.spec.md` - Escenario 4 | Covered |
| `REQ-P06` | Cancelar desde `MANAGED` o `IN_PREPARATION` | `transition-order.spec.md` - Escenarios 5, 6 y 9 | Covered |
| `API.md` | Consultar pedido | `get-order.spec.md`, `API.md` - `GET /orders/{orderId}` | Covered |

---

## Reglas de Negocio

| Fuente | Item | Verificacion | Estado |
|---|---|---|---|
| `RN-01` | `stockAvailable = stockTotal - stockReserved` | `QUALITY_GATES.md` - Responsabilidad de `Item` | Covered |
| `RN-02` | Stock no negativo | `QUALITY_GATES.md` - Responsabilidad de `Item` | Covered |
| `RN-03` | `stockTotal >= stockReserved` | `QUALITY_GATES.md` - Responsabilidad de `Item` | Covered |
| `RN-04` | Un solo estado y sin retroceso | `STATE_MACHINE.md`, `architecture.spec.md` - Rule 4 | Covered |
| `RN-05` | Solo transiciones operativas validas | `STATE_MACHINE.md`, `transition-order.spec.md`, `architecture.spec.md` - Rule 7 | Covered |

---

## Requisitos No Funcionales

| Fuente | Item | Verificacion | Estado |
|---|---|---|---|
| `NFR-IDP-01` | Idempotencia | `transition-order.spec.md` - Escenarios 7 a 12, `ADR-002`, `ADR-003` | Covered |
| `NFR-CON-01` | Consistencia entre `Order` e `Item` | `APPLICATION.md`, `QUALITY_GATES.md`, `persistence-concurrency.spec.md`, `016-implement-resilience-integration-tests.md` | Partial |
| `NFR-OBS-01` | `traceId` para trazabilidad operacional | `observability.spec.md`, `API.md`, `APPLICATION.md` - Trazabilidad Operacional | Covered |
| `NFR-AUD-01` | Auditabilidad minima | `create-order.spec.md` - Escenario 1, `transition-order.spec.md` - Escenarios 1 a 6 | Covered |

---

## Restricciones Tecnicas

| Fuente | Item | Verificacion | Estado |
|---|---|---|---|
| `RT-01` | Transaccion local unica | `APPLICATION.md`, `QUALITY_GATES.md`, `persistence-concurrency.spec.md`, `016-implement-resilience-integration-tests.md` | Partial |
| `RT-02` | REST sincronica y API-First | `API.md`, `project-structure.spec.md` - Rule 4 | Covered |
| `RT-03` | Arquitectura hexagonal | `architecture.spec.md` - Rules 1, 2 y 3 | Covered |
| `RT-04` | Java 21, Spring Boot, Maven, PostgreSQL, Docker, JUnit, Mockito | `project-structure.spec.md` - Rules 1 y 2 | Covered |
| `RT-05` | Piramide de testing y Given-When-Then | `project-structure.spec.md` - Rules 5, 6 y 7, `specs/architecture/*` | Covered |
| `VC-01` | Git y GitHub como base de control de versiones | `version-control.spec.md` - Rules 1 y 2, `017-configure-ci-and-pr-workflow.md` | Covered |
| `VC-02` | Ramas, PRs y worktrees para supervision multiagente | `version-control.spec.md` - Rules 3, 4, 5 y 7, `017-configure-ci-and-pr-workflow.md` | Covered |
| `AG-01` | Governance de agent tasks y politica anti-cascada | `agent-backlog-governance.spec.md`, `AGENT_BACKLOG_GOVERNANCE.md`, `AGENT_READINESS_REVIEW.md`, `AGENT_TASK_EXECUTION.md`, `AGENT_RUN_REPORT_TEMPLATE.md`, `AGENT_CONTEXT_PACKS.md`, `AGENT_RESOURCE_ACCOUNTING.md`, `018-implement-agent-backlog-governance.md` | Covered |
| `AG-02` | Toolchain minimo, roles de orquestacion y adopcion posterior de skills/tools/MCPs | `AGENT_TOOLCHAIN.md`, `AGENT_ORCHESTRATION_PLAYBOOK.md`, `AGENT_CONTEXT_PACKS.md`, `AGENT_RESOURCE_ACCOUNTING.md`, `version-control.spec.md` | Covered |

---

## ADRs

| Fuente | Decision | Verificacion | Estado |
|---|---|---|---|
| `ADR-001` | Locking pesimista para reserva de stock | `APPLICATION.md`, `QUALITY_GATES.md`, `persistence-concurrency.spec.md` - Escenario 1, `016-implement-resilience-integration-tests.md` | Partial |
| `ADR-002` | Locking optimista e idempotencia para transiciones | `transition-order.spec.md`, `QUALITY_GATES.md`, `persistence-concurrency.spec.md` - Escenarios 5, 6 y 7, `016-implement-resilience-integration-tests.md` | Partial |
| `ADR-003` | Modelado de transiciones con `OrderStateMachine` y `OrderTransitionRule` | `architecture.spec.md`, `transition-order.spec.md` | Covered |
| `ADR-005` | Limite transaccional en casos de uso de aplicacion | `APPLICATION.md`, `QUALITY_GATES.md`, `persistence-concurrency.spec.md`, `014-implement-transaction-boundaries-and-wiring.md`, `016-implement-resilience-integration-tests.md` | Partial |

---

## Gaps Actuales

| Gap | Motivo | Siguiente paso posible |
|---|---|---|
| Stack obligatorio aun no ejecutable | Existe spec de estructura, pero no scaffold | Ejecutar `002-create-project-scaffold.md` y verificar `mvn test` / `mvn verify` |
| Transacciones aun no ejecutables | Existe spec, pero no implementacion ni tests de integracion | Ejecutar `016-implement-resilience-integration-tests.md` cuando existan puertos/adaptadores |
| Locking pesimista/optimista aun no ejecutable | Existe spec, pero requiere persistencia real | Ejecutar `016-implement-resilience-integration-tests.md` con Testcontainers y PostgreSQL |

---

## Regla de Uso

Cuando se agregue una nueva spec, decision o requisito, este documento debe actualizarse.

Si aparece un `Gap`, no significa que el diseno este mal. Significa que todavia no existe una forma clara de comprobar automaticamente esa parte del sistema.
