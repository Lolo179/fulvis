# Spec - Architecture Constraints

**Estado:** Fase 1  
**Ultima revision:** 2026-08-18

---

## Proposito

Definir restricciones estructurales que una implementacion de Fulvis debe respetar.

Esta spec no describe comportamiento funcional de un caso de uso. Describe limites arquitectonicos verificables que pueden evolucionar hacia checks automaticos, por ejemplo ArchUnit, analisis estatico o reglas de harness.

---

## Rule 1 - Domain must not depend on external adapters

**Given**
- Existe codigo dentro del dominio de Fulvis

**When**
- Se revisan sus dependencias

**Then**
- No debe depender de controladores REST
- No debe depender de DTOs REST
- No debe depender de entidades JPA
- No debe depender de repositorios concretos
- No debe depender de frameworks web
- No debe depender de detalles de base de datos

---

## Rule 2 - Application depends on ports, not adapters

**Given**
- Existe codigo en la capa de aplicacion

**When**
- Se revisan las dependencias de los casos de uso

**Then**
- Los casos de uso pueden depender de puertos como `OrderRepositoryPort` e `ItemRepositoryPort`
- Los casos de uso no deben depender de adaptadores concretos de persistencia
- Los casos de uso no deben depender de controladores REST
- Los casos de uso no deben construir respuestas HTTP

---

## Rule 3 - REST adapters must not contain business decisions

**Given**
- Existe un adaptador REST

**When**
- Se revisa su responsabilidad

**Then**
- Debe traducir requests a comandos de aplicacion
- Debe traducir resultados y errores a respuestas HTTP
- No debe decidir si una transicion de estado es valida
- No debe calcular acciones de stock
- No debe modificar directamente `Order.status`

---

## Rule 4 - Order must not expose blind status mutation

**Given**
- Existe el agregado `Order`

**When**
- Se revisa su API publica

**Then**
- No debe exponer `setStatus(...)`
- No debe exponer metodos que permitan asignar un estado destino arbitrario
- Debe aplicar cambios de estado mediante una `OrderTransitionRule` ya resuelta
- Debe rechazar una `OrderTransitionRule` cuyo `from` no coincida con su `status`

---

## Rule 5 - Order must not operate on stock

**Given**
- Existe el agregado `Order`

**When**
- Se revisa su comportamiento

**Then**
- No debe ejecutar `reserve`
- No debe ejecutar `release`
- No debe ejecutar `decrement`
- No debe mantener referencia directa a `Item`
- Debe referenciar items solo mediante `itemId` en `OrderLine`

---

## Rule 6 - Item must own stock invariants

**Given**
- Existe el agregado `Item`

**When**
- Se revisa su comportamiento

**Then**
- Debe proteger `stockTotal >= 0`
- Debe proteger `stockReserved >= 0`
- Debe proteger `stockTotal >= stockReserved`
- Debe calcular `stockAvailable` como valor derivado
- Debe rechazar reservas superiores al stock disponible
- No debe depender de `Order`

---

## Rule 7 - OrderStateMachine must only resolve transition policy

**Given**
- Existe `OrderStateMachine`

**When**
- Se revisa su responsabilidad

**Then**
- Debe resolver `currentStatus + event`
- Puede devolver `APPLICABLE(rule)`, `ALREADY_SATISFIED` o `INVALID`
- No debe validar guardas internas de `Order`
- No debe ejecutar acciones de stock
- No debe persistir entidades
- No debe gestionar la creacion inicial de `Order`

---

## Rule 8 - Transition API must express events, not target status

**Given**
- Existe el endpoint `POST /orders/{orderId}/transitions`

**When**
- Se revisa su contrato

**Then**
- El request debe contener `event`
- El request no debe contener `status` destino
- El adaptador debe mapear el request a `TransitionOrderCommand`
- No debe existir un endpoint `PATCH /orders/{orderId}` para cambiar `status`

---

## Rule 9 - Technical vocabulary must follow CONVENTIONS.md

**Given**
- Existe una implementacion o documento tecnico de Fulvis

**When**
- Se revisa el vocabulario tecnico

**Then**
- Debe usar nombres tecnicos en ingles
- Debe usar los estados definidos en `CONVENTIONS.md`
- Debe usar los eventos definidos en `CONVENTIONS.md`
- No debe introducir estados, eventos, errores o agregados nuevos sin actualizar la documentacion correspondiente

---

## Rule 10 - Specs must not override accepted architecture

**Given**
- Se agrega una nueva spec

**When**
- La spec se revisa contra las fuentes de verdad de mayor alcance

**Then**
- No debe contradecir `REQUIREMENTS.md`
- No debe contradecir `DOMAIN.md`
- No debe contradecir `STATE_MACHINE.md`
- No debe contradecir ADRs aceptadas
- Si existe una tension, debe marcarse antes de implementar

---

## Quality Gates Relacionados

- El dominio no conoce infraestructura
- La aplicacion depende de puertos
- Los adaptadores no toman decisiones de negocio
- `Order` protege transiciones e historial
- `Item` protege inventario
- `OrderStateMachine` publica politica de transicion
- La API expresa eventos de negocio
