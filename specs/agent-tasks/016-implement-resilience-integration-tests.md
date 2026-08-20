# Agent Task 016 - Implement Resilience Integration Tests

**Estado:** Draft  
**Ultima revision:** 2026-08-19

---

## Task

```text
Nombre: Implement Resilience Integration Tests
Objetivo: Implementar pruebas de integracion que verifiquen atomicidad, rollback, concurrencia, locking e idempotencia con PostgreSQL real.
Tipo: testing
Rama sugerida: test/resilience-integration-tests
Worktree sugerido: worktrees/fulvis-resilience-integration-tests
```

---

## Contexto Obligatorio

El agente debe leer antes de trabajar:

```text
specs/CONVENTIONS.md
specs/REQUIREMENTS.md
specs/DOMAIN.md
specs/APPLICATION.md
specs/STATE_MACHINE.md
specs/QUALITY_GATES.md
specs/SPEC_COVERAGE.md
specs/adr/ADR-001-concurrencia-reserva-stock.md
specs/adr/ADR-002-concurrencia-transiciones-estado.md
specs/adr/ADR-003-modelado-transiciones-pedido.md
specs/adr/ADR-005-limite-transaccional-aplicacion.md
specs/architecture/persistence-concurrency.spec.md
specs/architecture/project-structure.spec.md
specs/architecture/architecture.spec.md
specs/agent-tasks/013-implement-persistence-adapters.md
specs/agent-tasks/014-implement-transaction-boundaries-and-wiring.md
specs/agent-tasks/015-implement-executable-quality-gates.md
```

---

## Fuentes de Verdad Aplicables

```text
Resilience:
- specs/architecture/persistence-concurrency.spec.md
- ADR-001 locking pesimista para Item
- ADR-002 locking optimista e idempotencia para Order
- ADR-005 limite transaccional en casos de uso

Application:
- CreateOrderUseCase
- TransitionOrderUseCase
- OrderRepositoryPort
- ItemRepositoryPort

Persistence:
- adaptadores persistentes de Order e Item
- PostgreSQL
- Testcontainers

Quality gates:
- Idempotencia y concurrencia
- Casos de uso
- Responsabilidad de Order
- Responsabilidad de Item
```

---

## Alcance

### Puede modificar

```text
- src/test/.../integration/**
- src/test/.../resilience/**
- src/test/.../adapters/out/persistence/**
- src/test/resources/**
- pom.xml solo si necesita perfiles o dependencias de integration tests
- README.md solo si necesita documentar comandos de integration tests
```

### No puede modificar

```text
- specs/** salvo para actualizar SPEC_COVERAGE.md si cambia cobertura
- src/main/.../domain/**
- src/main/.../application/**
- src/main/.../adapters/in/rest/**
- src/main/resources/openapi/**
```

### Fuera de alcance

```text
- Cambiar reglas de negocio
- Cambiar estados, eventos o transiciones
- Implementar nuevas funcionalidades
- Cambiar endpoints REST
- Cambiar contrato OpenAPI
- Sustituir PostgreSQL por una base de datos en memoria para estas pruebas
- Relajar invariantes para hacer pasar tests
```

---

## Restricciones

```text
- Respetar vocabulario tecnico en ingles definido en CONVENTIONS.md
- No modificar ADRs aceptadas sin decision explicita
- Las pruebas de resiliencia deben usar PostgreSQL real mediante Testcontainers cuando dependan de comportamiento de base de datos
- No usar una base de datos compartida manualmente
- No reemplazar locking real por mocks en pruebas de concurrencia
- No validar rollback solo con tests unitarios
- Las pruebas deben seguir formato Given-When-Then
- Las pruebas deben producir fallos claros y diagnosticables
- Marcar tensiones entre documentos antes de implementar
```

---

## Escenarios Obligatorios

### Resilience Scenario 1 - Concurrent stock reservation does not oversell

```text
Given un Item con stockTotal = 5 y stockReserved = 0
And dos requests concurrentes intentan crear pedidos para el mismo itemId
And cada request solicita quantity = 4
When ambas operaciones ejecutan CreateOrderUseCase concurrentemente
Then solo una operacion reserva stock correctamente
And la otra falla con InsufficientStock o espera hasta observar stock insuficiente
And stockReserved no supera stockTotal
And no se crean pedidos inconsistentes
```

---

### Resilience Scenario 2 - Create order rollback is atomic

```text
Given existe un Item con stock suficiente
When CreateOrderUseCase falla al persistir Order u OrderHistory
Then no se persiste Order
And no se persiste OrderHistory
And no queda incrementado stockReserved
And la transaccion completa hace rollback
```

---

### Resilience Scenario 3 - Cancel rollback is atomic

```text
Given existe un Order en MANAGED o IN_PREPARATION
And existe stock reservado para ese Order
When TransitionOrderUseCase con event = CANCEL falla al persistir cambio de estado o historial
Then Order no queda en CANCELLED
And no se persiste nuevo OrderHistory
And no queda liberado parcialmente el stock
And la transaccion completa hace rollback
```

---

### Resilience Scenario 4 - Ship rollback is atomic

```text
Given existe un Order en IN_PREPARATION
And existe stock reservado para ese Order
When TransitionOrderUseCase con event = MARK_SHIPPED falla al persistir cambio de estado o historial
Then Order no queda en SHIPPED
And no se persiste nuevo OrderHistory
And no queda decrementado parcialmente stockTotal
And no queda decrementado parcialmente stockReserved
And la transaccion completa hace rollback
```

---

### Resilience Scenario 5 - Concurrent same transition is idempotent

```text
Given existe un Order en MANAGED
And dos requests concurrentes intentan aplicar event = START_PREPARATION
When ambas operaciones ejecutan TransitionOrderUseCase concurrentemente
Then una operacion aplica la transicion a IN_PREPARATION
And la otra detecta conflicto optimista o relee estado actualizado
And la operacion repetida se considera ALREADY_SATISFIED
And no se crea historial duplicado
And no se produce estado inconsistente
```

---

### Resilience Scenario 6 - Concurrent different transitions do not duplicate effects

```text
Given existe un Order en MANAGED
And una request intenta aplicar event = START_PREPARATION
And otra request concurrente intenta aplicar event = CANCEL
When ambas operaciones compiten sobre el mismo Order
Then solo una transicion se confirma
And la operacion perdedora relee el estado actual
And si el estado actual no satisface su evento, falla con ConcurrencyConflict o InvalidOrderTransition
And no se duplican acciones de stock
And no se duplica historial
```

---

### Resilience Scenario 7 - Idempotent retry does not repeat stock action

```text
Given existe un Order que ya alcanzo el estado destino directo de un evento con accion de stock
When el cliente reintenta el mismo evento
Then la operacion devuelve exito idempotente
And no se repite la accion de stock
And no se crea nuevo OrderHistory
And el estado persistido permanece consistente
```

---

## Specs que Debe Satisfacer

```text
- specs/architecture/persistence-concurrency.spec.md
- specs/architecture/project-structure.spec.md
- specs/architecture/architecture.spec.md
```

---

## Quality Gates Aplicables

```text
- Reserva de stock usa locking pesimista
- Transiciones de Order usan locking optimista
- Operaciones que afectan Order e Item usan una unica transaccion local
- Reintentos idempotentes no duplican efectos
- Fallos de persistencia hacen rollback completo
- No production shortcut for tests
```

---

## Criterio de Done

La tarea se considera terminada cuando:

```text
- Existen integration tests de resiliencia con PostgreSQL/Testcontainers
- Los tests cubren reserva concurrente sin sobreventa
- Los tests cubren rollback atomico de creacion
- Los tests cubren rollback atomico de cancelacion
- Los tests cubren rollback atomico de envio
- Los tests cubren transicion concurrente idempotente
- Los tests cubren conflicto concurrente no satisfecho
- Los tests cubren reintento idempotente sin repetir stock
- Los tests verifican que no se duplica OrderHistory
- Los tests verifican que stockTotal y stockReserved quedan consistentes
- Los tests siguen formato Given-When-Then
- mvn verify ejecuta estos tests o existe perfil documentado para ejecutarlos
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
- integration tests de resiliencia
- ejecucion de mvn verify o perfil equivalente
- specs/architecture/persistence-concurrency.spec.md
- specs/adr/ADR-001-concurrencia-reserva-stock.md
- specs/adr/ADR-002-concurrencia-transiciones-estado.md
- specs/adr/ADR-005-limite-transaccional-aplicacion.md
- specs/QUALITY_GATES.md
- evidencia de PostgreSQL/Testcontainers para concurrencia y rollback
```

La revision humana debe centrarse en que la resiliencia quede demostrada por comportamiento real de infraestructura, no por mocks ni por confianza en anotaciones.
