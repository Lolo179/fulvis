# Agent Task 013 - Implement Persistence Adapters

**Estado:** Accepted

**Ultima revision:** 2026-11-25

---

## Task

```text
Nombre: Implement Persistence Adapters
Objetivo: Implementar adaptadores de persistencia para OrderRepositoryPort e ItemRepositoryPort respetando agregados, transacciones y estrategias de concurrencia.
Tipo: persistencia
Rama sugerida: feature/persistence-adapters
Worktree sugerido: worktrees/fulvis-persistence-adapters
```

---

## Nota de Orden

Esta tarea debe ejecutarse despues de `012-implement-rest-adapters.md` en estado `Accepted`.

Tensiones detectadas antes de implementar:

```text
- Order necesita una puerta explicita de rehidratacion para que el adaptador de persistencia no use reflection ni replay artificial de historial.
- Order necesita conservar un token de version persistente para que el locking optimista de ADR-002 sea real y no quede oculto por el adaptador.
- La relectura de ADR-002 necesita una transaccion nueva despues del rollback de la escritura.
```

Decisiones tecnicas aplicadas:

```text
- La rehidratacion y version se incorporan al dominio como soporte tecnico del agregado, no como datos de negocio ni como contrato REST.
- Las entidades JPA siguen viviendo solo en adapters/out/persistence.
- stockAvailable sigue siendo derivado y no se persiste.
- La tarea 013 cubre la recuperacion de conflictos de TransitionOrderUseCase. La 014 cubre el wiring general.
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
specs/architecture/architecture.spec.md
specs/architecture/project-structure.spec.md
specs/architecture/persistence-concurrency.spec.md
specs/agent-tasks/006-implement-create-order-use-case.md
specs/agent-tasks/007-implement-transition-order-use-case.md
```

---

## Fuentes de Verdad Aplicables

```text
Domain:
- Order
- OrderLine
- OrderHistory
- Item

Application:
- OrderRepositoryPort
- ItemRepositoryPort
- limite transaccional

ADRs:
- ADR-001 locking pesimista para reserva de stock
- ADR-002 locking optimista para transiciones de Order

Specs:
- specs/architecture/architecture.spec.md
- specs/architecture/project-structure.spec.md
- specs/architecture/persistence-concurrency.spec.md

Quality gates:
- Limites de dominio
- Responsabilidad de Order
- Responsabilidad de Item
- Idempotencia y concurrencia
```

---

## Alcance

### Puede modificar

```text
- src/main/.../adapters/out/persistence/**
- src/test/.../adapters/out/persistence/**
- src/main/.../domain/order/Order.java y OrderTest.java solo para rehidratacion y token de version
- src/main/.../application/order/{port,transition}/** y application/error/ConcurrencyConflictException.java solo para detectar y resolver el conflicto optimista de ADR-002
- src/test/.../application/order/** para verificar ese ajuste y adaptar dobles al puerto
- src/main/.../configuration/**
- src/main/resources/db/**
- src/main/resources/application*.yml
- pom.xml solo si necesita dependencias de persistencia o Testcontainers
```

### No puede modificar

```text
- specs/** salvo los metadatos de revision y alcance de esta ficha
- src/main/.../domain/** y src/main/.../application/** fuera de lo autorizado arriba
- src/main/.../adapters/in/rest/**
- src/main/resources/openapi/**
```

### Fuera de alcance

```text
- Cambiar reglas de negocio
- Cambiar estados, eventos o transiciones
- Cambiar endpoints REST
- Implementar controladores
- Cambiar contrato OpenAPI
- Introducir mensajeria o consistencia distribuida
- Introducir caches
```

---

## Restricciones

```text
- Respetar vocabulario tecnico en ingles definido en CONVENTIONS.md
- No modificar ADRs, specs ni quality gates
- El dominio no debe depender de JPA
- El dominio no debe depender de Spring Data
- Las entidades de persistencia no deben filtrarse hacia aplicacion o dominio
- Los repositorios concretos deben implementar puertos de aplicacion
- Order debe persistirse como agregado con lines e history
- OrderHistory debe ser append-only desde el punto de vista del dominio
- Item debe persistir stockTotal y stockReserved
- stockAvailable no debe persistirse como dato fuente
- ItemRepositoryPort.findAllByIdsForUpdate debe aplicar locking pesimista segun ADR-001
- Order debe tener versionado persistente para locking optimista segun ADR-002
- Los errores de infraestructura deben traducirse hacia errores conceptuales cuando corresponda
- Marcar tensiones entre documentos antes de implementar
```

---

## Persistencia Esperada

Modelo conceptual minimo:

```text
orders
- order_id
- status
- created_at
- updated_at
- version

order_lines
- order_id
- item_id
- quantity

order_history
- order_id
- previous_status
- new_status
- timestamp

items
- item_id
- reference
- name
- stock_total
- stock_reserved
```

Notas:

```text
- Los nombres fisicos pueden ajustarse a convenciones del proyecto, pero deben mantener trazabilidad conceptual.
- order_lines y order_history forman parte persistida del agregado Order.
- items representa un agregado independiente.
- stock_available se calcula como stock_total - stock_reserved.
```

---

## Estrategias de Concurrencia

```text
Item:
- Para operaciones que modifican stock, usar locking pesimista.
- La lectura debe bloquear las filas afectadas dentro de la transaccion.
- Objetivo: impedir sobreventa de stock.

Order:
- Usar locking optimista mediante version.
- Si aparece conflicto de version, la aplicacion debe poder aplicar la regla de idempotencia definida en ADR-002.
- Objetivo: evitar transiciones duplicadas sin bloquear el caso comun.
```

---

## Specs que Debe Satisfacer

```text
- specs/architecture/architecture.spec.md
- specs/architecture/project-structure.spec.md
- specs/architecture/persistence-concurrency.spec.md
```

---

## Quality Gates Aplicables

```text
- El dominio no conoce infraestructura
- La aplicacion depende de puertos
- Los adaptadores implementan puertos
- Reserva de stock usa locking pesimista
- Transiciones de Order usan locking optimista
- Operaciones que afectan Order e Item usan una unica transaccion local
- Fallos de persistencia hacen rollback completo
- Reintentos idempotentes no duplican efectos
```

---

## Criterio de Done

La tarea se considera terminada cuando:

```text
- Existe adaptador persistente para OrderRepositoryPort
- Existe adaptador persistente para ItemRepositoryPort
- Existen entidades o mappings de persistencia separados del dominio
- Order se persiste y recupera con lines e history
- Item se persiste y recupera con stockTotal y stockReserved
- stockAvailable se devuelve como valor derivado, no como fuente persistida
- ItemRepositoryPort.findAllByIdsForUpdate aplica lock pesimista
- Order usa versionado para locking optimista
- No hay anotaciones JPA en dominio
- No hay dependencias Spring Data en dominio
- Existen integration tests con PostgreSQL real/Testcontainers si la estructura del proyecto lo permite
- Los tests cubren reserva concurrente sin sobreventa
- Los tests cubren rollback atomico de creacion
- Los tests cubren rollback atomico de cancelacion o envio cuando aplique
- Los tests cubren conflicto optimista en Order cuando aplique
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
- integration tests de persistencia
- tests de concurrencia cuando existan
- specs/APPLICATION.md
- specs/DOMAIN.md
- specs/adr/ADR-001-concurrencia-reserva-stock.md
- specs/adr/ADR-002-concurrencia-transiciones-estado.md
- specs/architecture/persistence-concurrency.spec.md
- specs/QUALITY_GATES.md
- ausencia de JPA/Spring Data en dominio
```

La revision humana debe centrarse en que la persistencia sea un adaptador, no el modelo de dominio real, y en que concurrencia y atomicidad queden verificadas por pruebas.

## Historial de Cambios

- 2026-11-25: La revision autoriza la rehidratacion y version de Order y la recuperacion del conflicto optimista de ADR-002 dentro de la 013. El wiring general sigue en la 014.
- 2026-11-25: `mvn verify` paso 119 pruebas unitarias y 8 de integracion con PostgreSQL 18 y Testcontainers. La suite verifica concurrencia, rollback y conservacion de las filas de auditoria.
