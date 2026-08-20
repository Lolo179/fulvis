# Agent Task 011 - Create OpenAPI Contract

**Estado:** Draft  
**Ultima revision:** 2026-08-19

---

## Task

```text
Nombre: Create OpenAPI Contract
Objetivo: Crear el contrato OpenAPI/Swagger de Fulvis a partir de API.md, dejando la API externa documentada y verificable sin implementar controladores.
Tipo: API
Rama sugerida: feature/openapi-contract
Worktree sugerido: worktrees/fulvis-openapi-contract
```

---

## Contexto Obligatorio

El agente debe leer antes de trabajar:

```text
specs/CONVENTIONS.md
specs/REQUIREMENTS.md
specs/API.md
specs/APPLICATION.md
specs/STATE_MACHINE.md
specs/QUALITY_GATES.md
specs/SPEC_COVERAGE.md
specs/architecture/architecture.spec.md
specs/architecture/project-structure.spec.md
specs/architecture/observability.spec.md
specs/use-cases/create-order.spec.md
specs/use-cases/transition-order.spec.md
specs/use-cases/get-order.spec.md
specs/use-cases/get-item-stock.spec.md
specs/agent-tasks/010-implement-error-model-and-rest-mapping.md
```

---

## Fuentes de Verdad Aplicables

```text
Requirements:
- RT-02 REST sincronica y API-First
- NFR-OBS-01 traceId obligatorio

Application:
- CreateOrderCommand
- CreateOrderResult
- TransitionOrderCommand
- TransitionOrderResult
- errores conceptuales

API:
- Endpoints minimos
- Header obligatorio X-Trace-Id
- Requests y responses conceptuales
- Mapeo de errores
- Decision sobre transiciones

State machine:
- OrderEvent
- OrderStatus

Specs:
- specs/architecture/project-structure.spec.md
- specs/architecture/architecture.spec.md
- specs/architecture/observability.spec.md
- specs/use-cases/create-order.spec.md
- specs/use-cases/transition-order.spec.md
- specs/use-cases/get-order.spec.md
- specs/use-cases/get-item-stock.spec.md

Quality gates:
- API REST
- Vocabulario tecnico
- Observabilidad
```

---

## Alcance

### Puede modificar

```text
- src/main/resources/openapi/**
- src/test/.../openapi/**
- src/test/.../api/**
- pom.xml solo si necesita dependencias de validacion OpenAPI
```

### No puede modificar

```text
- specs/**
- src/main/.../domain/**
- src/main/.../application/**
- src/main/.../adapters/**
```

### Fuera de alcance

```text
- Implementar controladores REST
- Implementar casos de uso
- Implementar persistencia
- Cambiar endpoints definidos
- Cambiar eventos o estados
- Cambiar el modelo de errores conceptual
- Generar clientes externos
- Publicar documentacion en un servicio externo
```

---

## Restricciones

```text
- Respetar vocabulario tecnico en ingles definido en CONVENTIONS.md
- No modificar ADRs, specs ni quality gates
- No introducir endpoints no documentados
- No introducir estados, eventos o errores no documentados
- Documentar X-Trace-Id como header obligatorio en todas las operaciones
- POST /orders/{orderId}/transitions debe aceptar event, no status destino
- TransitionOrderResult.idempotent no debe exponerse necesariamente en la respuesta REST publica de Fase 1
- El contrato debe documentar respuestas de error estables
- El contrato no debe exponer stack traces ni detalles internos
- Marcar tensiones entre documentos antes de implementar
```

---

## Endpoints que Debe Documentar

```text
POST /orders
POST /orders/{orderId}/transitions
GET /orders/{orderId}
GET /items/{itemId}/stock
```

---

## Schemas Minimos Esperados

```text
CreateOrderRequest
CreateOrderLineRequest
CreateOrderResponse
TransitionOrderRequest
TransitionOrderResponse
GetOrderResponse
OrderLineResponse
OrderHistoryResponse
GetItemStockResponse
ErrorResponse
OrderStatus
OrderEvent
```

---

## Error Mapping Esperado

```text
400 Bad Request:
- InvalidOrderLines
- Missing X-Trace-Id

404 Not Found:
- OrderNotFound
- ItemNotFound

409 Conflict:
- InvalidOrderTransition
- InsufficientStock
- ConcurrencyConflict
```

---

## Specs que Debe Satisfacer

```text
- specs/architecture/project-structure.spec.md
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
- Debe existir un contrato OpenAPI/Swagger
- El contrato documenta los cuatro endpoints minimos
- El contrato documenta X-Trace-Id como obligatorio
- El contrato no permite enviar status destino en requests de transicion
- El contrato usa estados y eventos definidos en CONVENTIONS.md
- Errores conceptuales se mapean segun API.md
- La API expresa intenciones de negocio, no mutaciones internas
```

---

## Criterio de Done

La tarea se considera terminada cuando:

```text
- Existe un contrato OpenAPI en una ruta estable del proyecto
- El contrato define los cuatro endpoints minimos
- Todas las operaciones requieren X-Trace-Id
- POST /orders documenta request y response
- POST /orders/{orderId}/transitions documenta event y no acepta status destino
- GET /orders/{orderId} documenta lines e history
- GET /items/{itemId}/stock documenta stockTotal, stockReserved y stockAvailable
- ErrorResponse contiene error, message y traceId
- Los errores conceptuales documentados estan presentes en respuestas HTTP esperadas
- OrderStatus enum contiene solo estados documentados
- OrderEvent enum contiene solo eventos operativos aceptados para requests externas
- Existe una validacion automatica del contrato si la estructura del proyecto lo permite
- El agente reporta gaps, riesgos o tensiones detectadas
```

---

## Nota Sobre OrderEvent

`ORDER_CREATED` existe como evento conceptual de creacion e historial inicial, pero no es una transicion operativa solicitada por cliente externo.

Por tanto:

```text
- TransitionOrderRequest.event no debe aceptar ORDER_CREATED
- TransitionOrderRequest.event debe aceptar START_PREPARATION, MARK_SHIPPED, CONFIRM_RECEPTION, CLOSE y CANCEL
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
- contrato OpenAPI generado
- validacion automatica de OpenAPI si existe
- specs/API.md
- specs/APPLICATION.md
- specs/STATE_MACHINE.md
- specs/architecture/project-structure.spec.md
- specs/architecture/observability.spec.md
- specs/QUALITY_GATES.md
- ausencia de cambios conceptuales no autorizados
```

La revision humana debe centrarse en que el contrato externo sea fiel a las decisiones aceptadas y pueda actuar como frontera estable para agentes, tests de contrato y futuros adaptadores REST.
