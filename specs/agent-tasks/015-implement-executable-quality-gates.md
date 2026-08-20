# Agent Task 015 - Implement Executable Quality Gates

**Estado:** Draft  
**Ultima revision:** 2026-08-19

---

## Task

```text
Nombre: Implement Executable Quality Gates
Objetivo: Convertir quality gates y restricciones arquitectonicas en checks automaticos ejecutables por humanos, agentes y CI.
Tipo: testing
Rama sugerida: feature/executable-quality-gates
Worktree sugerido: worktrees/fulvis-executable-quality-gates
```

---

## Contexto Obligatorio

El agente debe leer antes de trabajar:

```text
specs/CONVENTIONS.md
specs/REQUIREMENTS.md
specs/DOMAIN.md
specs/STATE_MACHINE.md
specs/APPLICATION.md
specs/API.md
specs/QUALITY_GATES.md
specs/SPEC_COVERAGE.md
specs/adr/*
specs/architecture/architecture.spec.md
specs/architecture/project-structure.spec.md
specs/architecture/observability.spec.md
specs/architecture/persistence-concurrency.spec.md
specs/architecture/version-control.spec.md
specs/agent-tasks/010-implement-error-model-and-rest-mapping.md
specs/agent-tasks/011-create-openapi-contract.md
specs/agent-tasks/012-implement-rest-adapters.md
specs/agent-tasks/013-implement-persistence-adapters.md
specs/agent-tasks/014-implement-transaction-boundaries-and-wiring.md
```

---

## Fuentes de Verdad Aplicables

```text
Quality gates:
- specs/QUALITY_GATES.md

Architecture specs:
- specs/architecture/architecture.spec.md
- specs/architecture/project-structure.spec.md

Contracts:
- specs/API.md
- OpenAPI contract from Agent Task 011

ADRs:
- ADR-001
- ADR-002
- ADR-003
- ADR-005

Coverage:
- specs/SPEC_COVERAGE.md
```

---

## Alcance

### Puede modificar

```text
- pom.xml
- src/test/.../architecture/**
- src/test/.../quality/**
- src/test/.../openapi/**
- scripts/**
- .github/workflows/**
- README.md solo si necesita documentar comandos de verificacion
```

### No puede modificar

```text
- specs/** salvo para actualizar SPEC_COVERAGE.md si cambia cobertura
- src/main/.../domain/**
- src/main/.../application/**
- src/main/.../adapters/**
```

### Fuera de alcance

```text
- Cambiar reglas de negocio
- Cambiar arquitectura aceptada
- Implementar casos de uso
- Implementar controladores
- Implementar persistencia
- Cambiar contrato OpenAPI salvo tension documentada
- Exigir servicios externos manuales para checks
```

---

## Restricciones

```text
- Respetar vocabulario tecnico en ingles definido en CONVENTIONS.md
- No modificar ADRs aceptadas sin decision explicita
- No relajar quality gates para hacer pasar tests
- No convertir checks arquitectonicos en tests fragiles de implementacion interna
- Los checks deben ser ejecutables por un agente sin conocimiento tacito
- Los checks deben fallar con mensajes utiles
- Los checks no deben requerir inspeccion manual linea por linea
- Marcar tensiones entre documentos antes de implementar
```

---

## Checks Esperados

### Architecture Checks

```text
- Domain does not depend on Spring, JPA, HTTP, REST adapters or persistence adapters
- Application depends on ports, not adapters
- Application does not expose ResponseEntity or HTTP types
- REST adapters do not access persistence adapters directly
- REST adapters do not depend on JPA repositories
- Persistence adapters implement ports
- @Transactional does not appear in domain
- @Transactional does not appear in REST controllers
```

Implementacion sugerida:

```text
- ArchUnit si existe proyecto Java
- Tests estructurales equivalentes si ArchUnit no esta disponible
```

---

### Vocabulary Checks

```text
- No usar estados antiguos en castellano en codigo fuente
- No usar nombres tecnicos antiguos como PEDIDO, ITEM, LINEA_PEDIDO o PEDIDO_HISTORIAL
- Estados publicos coinciden con CONVENTIONS.md
- Eventos publicos coinciden con CONVENTIONS.md
```

---

### OpenAPI Checks

```text
- El contrato OpenAPI es valido
- Documenta POST /orders
- Documenta POST /orders/{orderId}/transitions
- Documenta GET /orders/{orderId}
- Documenta GET /items/{itemId}/stock
- Todas las operaciones requieren X-Trace-Id
- TransitionOrderRequest contiene event
- TransitionOrderRequest no contiene status destino
- ErrorResponse contiene error, message y traceId
```

---

### Test Command Checks

Debe existir una forma clara de ejecutar:

```text
- unit tests
- integration tests cuando existan
- architecture checks
- OpenAPI validation
- full verification
```

Nombre conceptual recomendado:

```text
mvn verify
```

Si se agregan scripts auxiliares, deben ser simples, versionados y documentados.

---

## Specs que Debe Satisfacer

```text
- specs/architecture/architecture.spec.md
- specs/architecture/project-structure.spec.md
- specs/architecture/observability.spec.md
- specs/architecture/version-control.spec.md
```

---

## Quality Gates Aplicables

```text
- Vocabulario tecnico
- Limites de dominio
- Transiciones de estado
- Casos de uso
- Idempotencia y concurrencia
- API REST
- Version control and agent workflow
```

---

## Criterio de Done

La tarea se considera terminada cuando:

```text
- Existen checks arquitectonicos ejecutables
- Existen checks de vocabulario ejecutables o equivalentes
- Existe validacion automatica del contrato OpenAPI
- mvn verify ejecuta la verificacion completa disponible
- Los checks fallan si dominio depende de Spring, JPA o HTTP
- Los checks fallan si aplicacion devuelve tipos HTTP
- Los checks fallan si REST accede directamente a repositorios de persistencia
- Los checks fallan si falta X-Trace-Id en OpenAPI
- Los checks fallan si TransitionOrderRequest acepta status destino
- Los comandos de verificacion estan documentados
- SPEC_COVERAGE.md se actualiza si cambia el estado de cobertura
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
- mvn verify
- tests de arquitectura
- validacion OpenAPI
- specs/QUALITY_GATES.md
- specs/architecture/architecture.spec.md
- specs/architecture/project-structure.spec.md
- specs/SPEC_COVERAGE.md
- claridad de mensajes de fallo
```

La revision humana debe centrarse en si los checks protegen las decisiones importantes sin sustituir criterio arquitectonico por burocracia inutil.
