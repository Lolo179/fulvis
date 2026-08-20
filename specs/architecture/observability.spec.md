# Spec - Observability

**Estado:** Fase 1  
**Ultima revision:** 2026-08-18

---

## Proposito

Definir el comportamiento esperado para trazabilidad operacional mediante `traceId`.

Esta spec pertenece al borde del sistema y a observabilidad. No introduce `traceId` en el dominio.

---

## Escenario 1 - Request externa con X-Trace-Id

**Given**
- Una request externa incluye el header `X-Trace-Id`

**When**
- El adaptador REST recibe la request

**Then**
- La request es aceptada para procesamiento
- El `traceId` se propaga a logs, trazas o contexto operacional
- El `traceId` no se incluye en `Order`
- El `traceId` no se incluye en `Item`
- El `traceId` no se incluye en `OrderHistory`

---

## Escenario 2 - Request externa sin X-Trace-Id

**Given**
- Una request externa no incluye el header `X-Trace-Id`

**When**
- El adaptador REST recibe la request

**Then**
- La request se rechaza con error de contrato
- No se ejecuta ningun caso de uso
- No se modifica `Order`
- No se modifica `Item`

---

## Escenario 3 - Error de negocio conserva traceId operacional

**Given**
- Una request externa incluye el header `X-Trace-Id`
- La operacion falla con un error de negocio, por ejemplo `InvalidOrderTransition`

**When**
- El adaptador REST traduce el error

**Then**
- La respuesta de error se puede correlacionar operacionalmente con el `traceId`
- El dominio no recibe `traceId` como dato de negocio
- No se crea `OrderHistory` por el solo hecho de existir un error

---

## Quality Gates Relacionados

- `traceId` es obligatorio en requests externas
- `traceId` pertenece a observabilidad operacional
- `traceId` no pertenece a `OrderHistory`
- Los adaptadores externos validan contrato de entrada
