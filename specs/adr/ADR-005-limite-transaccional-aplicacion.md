# ADR-005 - Ubicacion del limite transaccional

**Estado:** Aceptado  
**Fecha:** 2026-08-19  
**Autor:** Luis M.

---

## Contexto

Fulvis usa arquitectura hexagonal. El dominio debe permanecer libre de frameworks, persistencia y HTTP.

Los casos de uso de aplicacion coordinan agregados `Order` e `Item` mediante puertos. Varias operaciones requieren atomicidad:

- crear pedido y reservar stock
- cancelar pedido y liberar stock
- marcar pedido como enviado y decrementar stock
- persistir cambios de estado e historial en la misma transaccion

La implementacion prevista usa Java 21, Spring Boot, Maven y PostgreSQL.

---

## Problema

Hay que decidir donde se declara el limite transaccional para que agentes y desarrolladores no lo implementen de forma inconsistente.

Opciones habituales:

1. Declarar transacciones en controladores REST
2. Declarar transacciones en adaptadores de persistencia
3. Declarar transacciones en casos de uso de aplicacion
4. Crear un puerto propio de transacciones o decoradores transaccionales externos

La decision afecta a:

- atomicidad
- testabilidad
- acoplamiento a Spring
- claridad para agentes
- respeto de limites hexagonales

---

## Alternativas descartadas

| Alternativa | Motivo de descarte |
|---|---|
| Transaccion en controladores REST | Acopla el limite de negocio al transporte HTTP y dificulta reutilizar casos de uso desde CLI, harnesses o agentes |
| Transaccion en repositorios concretos | Fragmenta la unidad de trabajo; un repositorio no conoce la operacion completa entre `Order` e `Item` |
| Puerto `TransactionRunner` en Fase 1 | Mantiene mayor pureza, pero introduce una abstraccion adicional antes de necesitarla |
| Decoradores transaccionales externos en Fase 1 | Aumenta complejidad de wiring y supervision para poco beneficio inicial |

---

## Decision

En Fase 1, el limite transaccional se declara en los casos de uso de aplicacion que modifican conjuntamente agregados o requieren atomicidad de escritura.

Implementacion esperada:

```text
CreateOrderUseCase -> transaccional
TransitionOrderUseCase -> transaccional
GetOrderUseCase -> no requiere transaccion de escritura
GetItemStockUseCase -> no requiere transaccion de escritura
```

En una implementacion Spring Boot, esto puede expresarse con `@Transactional` sobre la clase o metodo del caso de uso concreto.

---

## Reglas de diseno

- `@Transactional` no puede aparecer en dominio.
- `@Transactional` no debe aparecer en controladores REST.
- Los controladores REST siguen siendo adaptadores delgados.
- Los repositorios concretos no definen la unidad completa de negocio.
- Los casos de uso no deben depender de HTTP, DTOs REST, `ResponseEntity` ni controladores.
- La aplicacion puede depender de Spring Transaction en Fase 1 como compromiso pragmatico.
- Si en el futuro se requiere una aplicacion totalmente libre de Spring, se revisara esta ADR y se evaluara un decorador transaccional o `TransactionRunner`.

---

## Justificacion

El caso de uso conoce la operacion completa y, por tanto, es el lugar mas claro para declarar la unidad atomica.

Esta decision mantiene:

- dominio limpio
- controladores REST delgados
- persistencia como adaptador
- atomicidad visible en el punto donde se orquestan agregados
- supervision sencilla para agentes mediante quality gates

El acoplamiento a Spring Transaction se acepta en Fase 1 porque el stack ya esta decidido y porque evita introducir una abstraccion prematura.

---

## Consecuencias

- La aplicacion no es completamente framework-agnostic en Fase 1.
- Los tests unitarios de dominio siguen sin Spring.
- Los tests de aplicacion pueden seguir usando mocks de puertos.
- Los tests de integracion deben verificar rollback real con PostgreSQL/Testcontainers.
- La migracion futura a decoradores transaccionales es posible si el proyecto exige mayor pureza arquitectonica.

---

## Relacion con otras decisiones

- `ADR-001` define locking pesimista para `Item` dentro de transaccion.
- `ADR-002` define locking optimista para `Order`.
- `ADR-003` define que el `Application Service` orquesta `Order` e `Item` dentro de una unica transaccion de negocio.
