# Agent Task 012 - Implement REST Adapters

**Estado:** Draft  
**Ultima revision:** 2026-08-19

---

## Task

```text
Nombre: Implement REST Adapters
Objetivo: Implementar controladores REST delgados que traduzcan requests HTTP a comandos de aplicacion y resultados/errores a respuestas HTTP, respetando el contrato OpenAPI.
Tipo: API
Rama sugerida: feature/rest-adapters
Worktree sugerido: worktrees/fulvis-rest-adapters
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
specs/architecture/observability.spec.md
specs/use-cases/create-order.spec.md
specs/use-cases/transition-order.spec.md
specs/use-cases/get-order.spec.md
specs/use-cases/get-item-stock.spec.md
specs/architecture/project-structure.spec.md
specs/agent-tasks/010-implement-error-model-and-rest-mapping.md
specs/agent-tasks/011-create-openapi-contract.md
```

---

## Fuentes de Verdad Aplicables

```text
API:
- Endpoints minimos
- Header obligatorio X-Trace-Id
- Requests y responses conceptuales
- Mapeo de errores
- Decision sobre transiciones

Application:
- CreateOrderUseCase
- TransitionOrderUseCase
- GetOrderUseCase
- GetItemStockUseCase
- Commands y results de aplicacion
- Errores conceptuales

OpenAPI:
- Contrato generado por Agent Task 011

Architecture:
- REST adapters must not contain business decisions
- Application depends on ports, not adapters

Quality gates:
- API REST
- Limites de dominio
- Casos de uso
- Observabilidad
```

---

## Alcance

### Puede modificar

```text
- src/main/.../adapters/in/rest/**
- src/test/.../adapters/in/rest/**
- src/main/.../api/**
- src/test/.../api/**
- src/main/.../configuration/** solo si es necesario para registrar beans del adaptador REST
```

### No puede modificar

```text
- specs/**
- src/main/.../domain/**
- src/main/.../application/** salvo para usar contratos ya existentes
- src/main/.../adapters/out/persistence/**
- src/main/resources/openapi/** salvo para corregir desalineaciones menores detectadas contra API.md
```

### Fuera de alcance

```text
- Implementar reglas de negocio
- Implementar persistencia
- Cambiar commands o results de aplicacion sin tension documentada
- Cambiar estados, eventos o transiciones
- Cambiar el contrato OpenAPI de forma conceptual
- Crear endpoints no documentados
- Introducir autenticacion o autorizacion
```

---

## Restricciones

```text
- Respetar vocabulario tecnico en ingles definido en CONVENTIONS.md
- Los controladores REST deben ser adaptadores delgados
- Los controladores REST no deben contener reglas de negocio
- Los controladores REST no deben decidir si una transicion es valida
- Los controladores REST no deben calcular acciones de stock
- Los controladores REST no deben modificar Order.status directamente
- Los controladores REST deben delegar en casos de uso de aplicacion
- Los DTOs REST no deben filtrarse hacia dominio
- Los casos de uso no deben devolver ResponseEntity ni tipos HTTP
- El dominio no debe depender de Spring Web
- X-Trace-Id debe validarse antes de ejecutar casos de uso
- traceId no debe entrar en Order, Item ni OrderHistory
- Marcar tensiones entre documentos antes de implementar
```

---

## Endpoints a Implementar

```text
POST /orders
POST /orders/{orderId}/transitions
GET /orders/{orderId}
GET /items/{itemId}/stock
```

---

## Responsabilidad de los Controladores

Los controladores REST solo pueden hacer:

```text
1. Leer path variables, headers y body
2. Validar contrato externo minimo, incluyendo X-Trace-Id
3. Mapear DTO REST -> command de aplicacion
4. Invocar el caso de uso correspondiente
5. Mapear result de aplicacion -> DTO REST
6. Delegar errores al mapper REST definido en Agent Task 010
```

Los controladores REST no pueden hacer:

```text
1. Consultar repositorios directamente
2. Crear o modificar agregados directamente
3. Calcular stockAvailable si ya pertenece al result de aplicacion
4. Resolver OrderStateMachine
5. Crear OrderHistory
6. Decidir idempotencia
7. Cambiar estados
```

---

## Mapeos Esperados

```text
POST /orders:
REST request -> CreateOrderCommand
CreateOrderResult -> CreateOrderResponse

POST /orders/{orderId}/transitions:
REST request + path orderId -> TransitionOrderCommand
TransitionOrderResult -> TransitionOrderResponse

GET /orders/{orderId}:
path orderId -> GetOrderUseCase input
GetOrderResult -> GetOrderResponse

GET /items/{itemId}/stock:
path itemId -> GetItemStockUseCase input
GetItemStockResult -> GetItemStockResponse
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
- specs/architecture/project-structure.spec.md
```

---

## Quality Gates Aplicables

```text
- Toda request externa debe incluir X-Trace-Id
- Existe POST /orders
- Existe POST /orders/{orderId}/transitions
- Existe GET /orders/{orderId}
- Existe GET /items/{itemId}/stock
- No se usa PATCH /orders/{orderId} para cambiar status
- El cliente envia event, no status destino
- Errores conceptuales se mapean segun API.md
- Los adaptadores externos no toman decisiones de negocio
- Los casos de uso no construyen respuestas HTTP
- El dominio no conoce HTTP
```

---

## Criterio de Done

La tarea se considera terminada cuando:

```text
- Existen controladores REST para los cuatro endpoints minimos
- Los controladores delegan en los casos de uso correspondientes
- Los controladores validan X-Trace-Id antes de ejecutar casos de uso
- POST /orders mapea correctamente request y response
- POST /orders/{orderId}/transitions acepta event y no status destino
- GET /orders/{orderId} devuelve lines e history
- GET /items/{itemId}/stock devuelve stockTotal, stockReserved y stockAvailable
- Los errores se traducen mediante el mapper REST
- No hay ResponseEntity ni tipos HTTP en aplicacion
- No hay dependencias Spring Web en dominio
- Existen tests de controlador o adapter REST en formato Given-When-Then
- Los tests verifican delegacion a casos de uso
- Los tests verifican ausencia de X-Trace-Id
- Los tests verifican que transition request no acepta status destino
- Los tests verifican mapeo de errores principales
- El contrato OpenAPI sigue alineado
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
- tests de controladores/adaptadores REST
- contrato OpenAPI
- specs/API.md
- specs/APPLICATION.md
- specs/architecture/architecture.spec.md
- specs/architecture/observability.spec.md
- specs/QUALITY_GATES.md
- ausencia de reglas de negocio en controladores
- ausencia de dependencias HTTP en dominio y aplicacion
```

La revision humana debe centrarse en que la API sea un borde limpio: traduce, delega y protege el contrato, pero no decide negocio.
