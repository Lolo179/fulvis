# Agent Task 002 - Create Project Scaffold

**Estado:** Accepted  
**Ultima revision:** 2026-08-20

---

## Task

```text
Nombre: Create Project Scaffold
Objetivo: Crear el scaffold inicial de Fulvis como proyecto Java 21 Spring Boot Maven con estructura hexagonal minima y comandos de verificacion.
Tipo: aplicacion
Rama sugerida: feature/project-scaffold
Worktree sugerido: worktrees/fulvis-project-scaffold
```

---

## Nota de Orden

Esta tarea debe ejecutarse despues de `001-migrate-docs-to-specs-structure.md` y antes de cualquier tarea de implementacion de codigo.

Si el repositorio aun no contiene codigo fuente, esta tarea debe ejecutarse antes de:

```text
- 003-implement-order-domain.md
- 004-implement-order-state-machine.md
- 005-implement-item-domain.md
- 006-implement-create-order-use-case.md
- 007-implement-transition-order-use-case.md
- 008-implement-get-item-stock-use-case.md
- 009-implement-get-order-use-case.md
- 010-implement-error-model-and-rest-mapping.md
- 011-create-openapi-contract.md
- 012-implement-rest-adapters.md
- 013-implement-persistence-adapters.md
- 014-implement-transaction-boundaries-and-wiring.md
- 015-implement-executable-quality-gates.md
```

---

## Contexto Obligatorio

El agente debe leer antes de trabajar:

```text
specs/CONVENTIONS.md
specs/REQUIREMENTS.md
specs/QUALITY_GATES.md
specs/SPEC_COVERAGE.md
specs/architecture/project-structure.spec.md
specs/architecture/architecture.spec.md
specs/architecture/version-control.spec.md
specs/adr/ADR-004-estructura-sdd-harness.md
specs/adr/ADR-005-limite-transaccional-aplicacion.md
specs/agent-profiles/executor-agent.md
```

---

## Fuentes de Verdad Aplicables

```text
Project structure:
- Java 21
- Spring Boot
- Maven
- PostgreSQL readiness
- Docker readiness
- JUnit
- Mockito
- Testcontainers where useful

Architecture:
- domain
- application
- adapters/in/rest
- adapters/out/persistence

Version control:
- Git
- GitHub
- branches
- PRs
- worktrees

Quality gates:
- Stack obligatorio
- Arquitectura hexagonal
- Unit tests
- Integration tests
- API-First

Agent workflow:
- 001-migrate-docs-to-specs-structure.md debe estar Accepted
- Executor Agent
- Reviewer Agent para revision posterior
```

---

## Alcance

### Puede modificar

```text
- pom.xml
- src/main/**
- src/test/**
- src/main/resources/**
- Dockerfile
- docker-compose.yml
- .gitignore
- README.md
- .github/workflows/**
```

### No puede modificar

```text
- specs/** salvo para actualizar SPEC_COVERAGE.md si cambia cobertura
- reglas de negocio
- ADRs aceptadas
- specs existentes
```

### Fuera de alcance

```text
- Implementar dominio real
- Implementar casos de uso reales
- Implementar controladores reales
- Implementar persistencia real
- Crear OpenAPI completo
- Cambiar decisiones arquitectonicas
- Crear funcionalidades no documentadas
```

---

## Restricciones

```text
- Respetar vocabulario tecnico en ingles definido en CONVENTIONS.md
- No modificar ADRs aceptadas sin decision explicita
- No introducir estados, eventos, errores o agregados nuevos
- El scaffold no debe contener logica de negocio falsa
- El dominio no debe depender de Spring
- El dominio no debe depender de JPA
- La aplicacion debe poder depender de puertos
- Los adaptadores deben quedar separados en in/rest y out/persistence
- El proyecto debe permitir ejecutar tests desde Maven
- El scaffold debe ser minimo pero extensible
- Marcar tensiones entre documentos antes de implementar
```

---

## Estructura Esperada

Estructura conceptual minima:

```text
pom.xml
src/
  main/
    java/
      .../
        FulvisApplication.java
        domain/
        application/
        adapters/
          in/
            rest/
          out/
            persistence/
        configuration/
    resources/
      application.yml
  test/
    java/
      .../
        architecture/
        domain/
        application/
        adapters/
```

Notas:

```text
- El package base debe ser estable y coherente.
- Evitar nombres genericos como demo si no representan el proyecto.
- No crear clases de negocio vacias que simulen implementacion.
- Se permiten paquetes vacios solo si el sistema de build los tolera; si no, crear placeholders minimos sin logica de negocio.
```

---

## Dependencias Minimas Esperadas

```text
- Spring Boot starter
- Spring Boot starter web
- Spring Boot starter validation
- Spring Boot starter data jpa
- PostgreSQL driver
- Spring Boot starter test
- Mockito incluido por starter test o explicitamente si aplica
- Testcontainers PostgreSQL
- ArchUnit si se implementan checks arquitectonicos desde el scaffold
```

La version exacta debe elegirse de forma compatible y estable con Java 21.

---

## Comandos Esperados

Debe existir una forma clara de ejecutar:

```text
mvn test
mvn verify
```

Si se definen perfiles:

```text
- unit
- integration
```

deben estar documentados en `README.md`.

---

## Specs que Debe Satisfacer

```text
- specs/architecture/project-structure.spec.md
- specs/architecture/architecture.spec.md
- specs/architecture/version-control.spec.md
```

---

## Quality Gates Aplicables

```text
- Stack obligatorio
- Arquitectura hexagonal
- Dominio aislado
- Aplicacion depende de puertos
- Adaptadores implementan puertos o traducen protocolos externos
- Unit tests ejecutables
- Integration tests preparados para Testcontainers
- Version control and agent workflow
```

---

## Criterio de Done

La tarea se considera terminada cuando:

```text
- Existe pom.xml
- El proyecto declara Java 21
- Existe clase de arranque Spring Boot
- Existe estructura hexagonal minima
- Existe configuracion base de aplicacion
- Existe preparacion para PostgreSQL
- Existe preparacion para Docker
- Existen dependencias de testing
- mvn test ejecuta correctamente
- mvn verify ejecuta correctamente o reporta gaps justificados
- .gitignore evita artefactos de build y entorno local
- README.md documenta comandos basicos
- No se introduce logica de negocio falsa
- No se modifica specs/** salvo SPEC_COVERAGE.md si aplica
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
- mvn test
- mvn verify
- specs/architecture/project-structure.spec.md
- specs/architecture/architecture.spec.md
- specs/QUALITY_GATES.md
- ausencia de logica de negocio falsa
- estructura de paquetes
```

La revision humana debe centrarse en que el scaffold cree un carril limpio para agentes posteriores, no en que anticipe implementaciones que aun pertenecen a otras tasks.
