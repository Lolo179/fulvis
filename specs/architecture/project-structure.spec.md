# Spec - Project Structure

**Estado:** Fase 1  
**Ultima revision:** 2026-08-18

---

## Proposito

Definir la estructura minima esperada para una futura implementacion de Fulvis.

Esta spec no crea el proyecto. Define restricciones verificables para que un agente o desarrollador no improvise el scaffold.

---

## Rule 1 - Required stack

**Given**
- Existe una implementacion de Fulvis

**When**
- Se revisa la estructura del proyecto

**Then**
- Debe usar Java 21
- Debe usar Spring Boot
- Debe usar Maven
- Debe estar preparado para PostgreSQL
- Debe estar preparado para Docker
- Debe usar JUnit para testing
- Debe usar Mockito donde aporte valor en unit tests
- Debe usar Testcontainers donde aporte valor en integration tests

---

## Rule 2 - Maven project

**Given**
- Existe una implementacion de Fulvis

**When**
- Se revisa el build

**Then**
- Debe existir `pom.xml`
- El build debe declarar Java 21
- El build debe permitir ejecutar tests unitarios
- El build debe permitir ejecutar tests de integracion
- El build no debe requerir servicios externos manuales para tests de integracion cuando Testcontainers sea aplicable

---

## Rule 3 - Hexagonal package structure

**Given**
- Existe una implementacion de Fulvis

**When**
- Se revisa la organizacion del codigo

**Then**
- Debe separar dominio, aplicacion y adaptadores
- El dominio no debe depender de Spring
- El dominio no debe depender de JPA
- La aplicacion debe depender de puertos
- Los adaptadores deben implementar puertos o traducir protocolos externos

Estructura conceptual esperada:

```text
domain
application
adapters
  in
    rest
  out
    persistence
```

---

## Rule 4 - API-First contract

**Given**
- Existe una implementacion de Fulvis con API REST

**When**
- Se revisa el contrato externo

**Then**
- Debe existir un contrato OpenAPI/Swagger
- El contrato debe documentar `POST /orders`
- El contrato debe documentar `POST /orders/{orderId}/transitions`
- El contrato debe documentar `GET /orders/{orderId}`
- El contrato debe documentar `GET /items/{itemId}/stock`
- El contrato debe documentar el header obligatorio `X-Trace-Id`
- El contrato no debe permitir enviar `status` destino en requests de transicion

---

## Rule 5 - Unit tests

**Given**
- Existe una implementacion de Fulvis

**When**
- Se revisan los tests unitarios

**Then**
- Deben cubrir invariantes de `Order`
- Deben cubrir invariantes de `Item`
- Deben cubrir `OrderStateMachine`
- Deben seguir formato Given-When-Then
- No deben requerir base de datos real
- No deben requerir contenedores

---

## Rule 6 - Integration tests

**Given**
- Existe una implementacion de Fulvis

**When**
- Se revisan los tests de integracion

**Then**
- Deben cubrir adaptadores de persistencia cuando existan
- Deben cubrir comportamiento transaccional cuando exista persistencia real
- Deben cubrir locking pesimista para reserva de stock
- Deben cubrir locking optimista para transiciones de `Order`
- Deben usar Testcontainers cuando la verificacion dependa de PostgreSQL real
- Deben seguir formato Given-When-Then

---

## Rule 7 - No production shortcut for tests

**Given**
- Existe una implementacion de Fulvis

**When**
- Se revisa la estrategia de testing

**Then**
- Los tests unitarios no deben depender de infraestructura externa
- Los tests de integracion no deben usar una base de datos compartida manualmente
- Los tests no deben relajar invariantes de dominio para facilitar implementacion

---

## Quality Gates Relacionados

- Stack obligatorio
- Arquitectura hexagonal
- API-First mediante OpenAPI/Swagger
- Unit tests para dominio y aplicacion
- Integration tests para persistencia, transacciones y concurrencia
- Given-When-Then como formato de pruebas
