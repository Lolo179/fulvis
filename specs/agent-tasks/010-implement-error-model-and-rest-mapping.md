# Agent Task 010 - Implement Error Model and REST Mapping

**Estado:** Draft  
**Ultima revision:** 2026-08-19

---

## Task

```text
Nombre: Implement Error Model and REST Mapping
Objetivo: Implementar un modelo coherente de errores conceptuales y su traduccion a respuestas HTTP en el adaptador REST.
Tipo: API
Rama sugerida: feature/error-model-rest-mapping
Worktree sugerido: worktrees/fulvis-error-model-rest-mapping
```

---

## Contexto Obligatorio

El agente debe leer antes de trabajar:

```text
specs/CONVENTIONS.md
specs/REQUIREMENTS.md
specs/APPLICATION.md
specs/API.md
specs/QUALITY_GATES.md
specs/SPEC_COVERAGE.md
specs/architecture/architecture.spec.md
specs/architecture/observability.spec.md
specs/use-cases/create-order.spec.md
specs/use-cases/transition-order.spec.md
specs/use-cases/get-order.spec.md
specs/use-cases/get-item-stock.spec.md
```

---

## Fuentes de Verdad Aplicables

```text
Application:
- Errores conceptuales
- Clasificacion por responsabilidad
- Regla de diseno para adaptadores externos

API:
- Header obligatorio X-Trace-Id
- Mapeo de errores

Specs:
- specs/architecture/architecture.spec.md
- specs/architecture/observability.spec.md
- specs/use-cases/create-order.spec.md
- specs/use-cases/transition-order.spec.md
- specs/use-cases/get-order.spec.md
- specs/use-cases/get-item-stock.spec.md

Quality gates:
- API REST
- Casos de uso
- Limites de dominio
- Observabilidad
```

---

## Alcance

### Puede modificar

```text
- src/main/.../application/**
- src/main/.../api/**
- src/main/.../rest/**
- src/test/.../application/**
- src/test/.../api/**
- src/test/.../rest/**
```

### No puede modificar

```text
- specs/**
- src/main/.../domain/** salvo para usar errores de dominio ya previstos
- adaptadores de persistencia
- configuracion de base de datos
```

### Fuera de alcance

```text
- Crear endpoints completos si aun no existen
- Implementar persistencia
- Implementar OpenAPI/Swagger
- Cambiar reglas de negocio
- Crear nuevos errores conceptuales no documentados
- Modificar estados, eventos o transiciones
- Introducir traceId en Order, Item u OrderHistory
```

---

## Restricciones

```text
- Respetar vocabulario tecnico en ingles definido en CONVENTIONS.md
- No modificar ADRs, specs ni quality gates
- No introducir errores conceptuales nuevos sin decision explicita
- El dominio no debe conocer HTTP
- La aplicacion no debe construir respuestas HTTP
- El adaptador REST es responsable de traducir errores a HTTP
- La ausencia de X-Trace-Id debe rechazarse antes de ejecutar casos de uso
- Las respuestas de error deben permitir correlacion operacional mediante traceId
- traceId no debe entrar en Order, Item ni OrderHistory
- Marcar tensiones entre documentos antes de implementar
```

---

## Error Mapping Esperado

```text
OrderNotFound -> 404 Not Found
ItemNotFound -> 404 Not Found
InvalidOrderTransition -> 409 Conflict
InsufficientStock -> 409 Conflict
InvalidOrderLines -> 400 Bad Request
ConcurrencyConflict -> 409 Conflict
Missing X-Trace-Id -> 400 Bad Request
```

---

## Contrato Conceptual de Error

La respuesta de error debe ser estable para clientes, tests y harnesses.

Forma conceptual recomendada:

```json
{
  "error": "InvalidOrderTransition",
  "message": "Order transition is not valid for the current status",
  "traceId": "trace-123"
}
```

Notas:

```text
- error contiene el codigo conceptual estable
- message puede ser legible, pero no debe ser la unica fuente verificable
- traceId pertenece a observabilidad operacional
- No incluir detalles internos de persistencia, stack traces o nombres de clases de infraestructura
```

---

## Specs que Debe Satisfacer

```text
- specs/architecture/architecture.spec.md
- specs/architecture/observability.spec.md
- specs/use-cases/create-order.spec.md
- specs/use-cases/transition-order.spec.md
- specs/use-cases/get-order.spec.md
- specs/use-cases/get-item-stock.spec.md
```

---

## Quality Gates Aplicables

```text
- Errores conceptuales se mapean segun API.md
- Los adaptadores REST no contienen decisiones de negocio
- Los casos de uso no construyen respuestas HTTP
- El dominio no depende de HTTP
- X-Trace-Id es obligatorio en requests externas
- Un error de negocio conserva correlacion operacional con traceId
- No se crea OrderHistory por el solo hecho de existir un error
```

---

## Criterio de Done

La tarea se considera terminada cuando:

```text
- Existe un modelo de errores conceptuales reutilizable
- Los errores documentados estan representados de forma explicita
- Existe un mapper REST de errores
- El mapper REST aplica el mapeo definido en API.md
- La falta de X-Trace-Id produce 400 Bad Request
- Los casos de uso no construyen respuestas HTTP
- El dominio no depende de HTTP
- Las respuestas de error incluyen codigo conceptual estable
- Las respuestas de error permiten correlacion con traceId
- No se exponen stack traces ni detalles internos
- Existen tests unitarios del mapper de errores
- Existen tests para ausencia de X-Trace-Id
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
- tests del mapper REST
- tests de X-Trace-Id obligatorio
- specs/APPLICATION.md
- specs/API.md
- specs/architecture/architecture.spec.md
- specs/architecture/observability.spec.md
- specs/QUALITY_GATES.md
- ausencia de cambios en specs/**
```

La revision humana debe centrarse en que los errores sigan siendo conceptuales, el HTTP permanezca en el borde y el contrato sea verificable por tests y harnesses.
