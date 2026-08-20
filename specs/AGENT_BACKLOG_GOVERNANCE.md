# AGENT_BACKLOG_GOVERNANCE.md - Fulvis

**Estado:** Fase 1  
**Ultima revision:** 2026-08-19

---

## Proposito

Este documento define como se gobierna el backlog de agent tasks en Fulvis.

Su objetivo es evitar errores en cascada cuando una task falla, queda incompleta, descubre una tension o necesita cambiar despues de haber sido creada.

Este documento esta pensado para humanos, agentes, harnesses, tools y skills.

---

## Principio Central

Una agent task no es una orden irreversible.

Una task es una unidad versionada, revisable y corregible. Si falla, no se debe continuar automaticamente con tasks dependientes sin evaluar el impacto.

El objetivo no es que el agente nunca falle. El objetivo es que el sistema de trabajo sepa:

- detectar el fallo
- clasificarlo
- recuperar contexto
- corregir con trazabilidad
- reintentar solo cuando sea seguro
- escalar cuando haya una decision nueva

---

## Estados Permitidos

Cada agent task debe tener uno de estos estados:

```text
Draft
Ready
In Progress
Blocked
Needs Revision
Accepted
Superseded
```

### Draft

La task esta en diseno. Puede cambiar sin considerarse una ruptura.

Uso esperado:

```text
- backlog inicial
- discovery arquitectonico
- tasks aun no listas para agentes
```

### Ready

La task esta lista para ser ejecutada por un agente.

Requisitos para pasar a `Ready`:

```text
- tiene objetivo claro
- define contexto obligatorio
- define alcance
- define restricciones
- define specs aplicables
- define quality gates aplicables
- define criterio de done
- no depende implicitamente de una task posterior
- no contradice ADRs aceptadas
```

### In Progress

La task esta siendo ejecutada por un agente o humano.

Reglas:

```text
- no modificar la task en paralelo sin coordinar
- registrar tensiones detectadas
- no ampliar alcance sin autorizacion
```

### Blocked

La task no puede avanzar sin una decision o dependencia externa.

Ejemplos:

```text
- falta una decision arquitectonica
- falta una dependencia previa
- falta acceso a una herramienta
- existe contradiccion entre documentos
```

### Needs Revision

La task es ejecutable en parte, pero se descubrio que necesita ajuste.

Ejemplos:

```text
- el criterio de done es incompleto
- falta una spec aplicable
- el orden del backlog es incorrecto
- el agente detecta un caso no cubierto
```

### Accepted

La task fue completada y aceptada por supervision.

Requisitos:

```text
- cumple criterio de done
- reporta validaciones ejecutadas
- reporta gaps conocidos
- no contradice fuentes de verdad
- pasa quality gates disponibles
```

### Superseded

La task fue reemplazada por otra task o decision posterior.

Reglas:

```text
- no borrar la task sin motivo
- indicar que task o ADR la reemplaza
- actualizar referencias cruzadas
```

---

## Politica Anti-Cascada

No se debe ejecutar una task dependiente si una task previa requerida esta en:

```text
Blocked
Needs Revision
```

Excepcion:

```text
Solo se puede continuar si el supervisor declara explicitamente que la dependencia no afecta al nuevo trabajo.
```

Ejemplo:

```text
No ejecutar CI si aun no existen quality gates ejecutables o resilience integration tests, salvo que CI se configure de forma parcial y lo declare como gap.
```

---

## Protocolo de Fallo Durante Ejecucion

Si una task falla, el agente debe detener la ejecucion normal y reportar:

```text
Task:
Estado propuesto:
Punto donde fallo:
Comando ejecutado:
Error observado:
Docs leidos:
Tension detectada:
Cambio propuesto:
Riesgo si se continua:
Puede reintentarse de forma segura: si/no
```

El agente puede intentar una correccion automatica solo si:

```text
- el fallo es tecnico y local
- no cambia arquitectura
- no cambia reglas de negocio
- no cambia contrato externo
- no modifica ADRs aceptadas
- no amplia el alcance de la task
```

Ejemplos de correccion automatica permitida:

```text
- arreglar un import
- corregir formato
- corregir nombre de archivo
- ajustar un test roto por typo
- reintentar un comando despues de limpiar un artefacto temporal
```

Ejemplos que requieren supervision:

```text
- cambiar una regla de negocio
- cambiar una transicion de estado
- cambiar el contrato REST
- relajar un quality gate
- cambiar una ADR
- saltarse una dependencia previa
```

---

## Reintentos

Un agente puede reintentar una operacion fallida si:

```text
- entiende la causa probable
- el cambio de correccion esta dentro del alcance
- el reintento no oculta el fallo original
- el output final indica que hubo reintento
```

Limite recomendado:

```text
maxRetries = 2
```

Si falla despues de dos reintentos, la task debe pasar a:

```text
Blocked
```

o:

```text
Needs Revision
```

segun la causa.

---

## Actualizacion de Tasks

Una task puede actualizarse cuando:

```text
- se detecta ambiguedad
- se detecta dependencia incorrecta
- aparece una nueva spec aplicable
- aparece un quality gate nuevo
- se descubre una tension con una ADR
- cambia el orden recomendado de ejecucion
```

Toda actualizacion debe:

```text
- mantener coherencia entre nombre de archivo y titulo
- actualizar referencias cruzadas
- actualizar SPEC_COVERAGE.md si afecta cobertura
- respetar CONVENTIONS.md
- no ocultar que existio el cambio
```

Si el cambio es relevante, agregar una seccion:

```text
## Historial de Cambios
```

---

## Cambios en Specs o ADRs

Si una task descubre una regla nueva de comportamiento:

```text
Actualizar spec aplicable.
```

Si una task descubre una decision arquitectonica nueva:

```text
Crear o actualizar ADR.
```

Si una task cambia cobertura:

```text
Actualizar SPEC_COVERAGE.md.
```

Si una task solo corrige wording, typos o referencias:

```text
No requiere ADR.
```

---

## Salida Esperada de un Agente

Toda ejecucion de task debe terminar con:

```text
Resumen del cambio:

Task ejecutada:

Estado final propuesto:

Docs leidos:

Specs satisfechas:

Quality gates relevantes:

Archivos modificados:

Validaciones ejecutadas:

Reintentos realizados:

Tensiones detectadas:

Gaps restantes:

Riesgo de cascada:
```

---

## Regla Para Harness, Tools y Skills

Un harness que ejecute tasks debe implementar este flujo conceptual:

```text
1. Leer task
2. Verificar estado Ready
3. Leer contexto obligatorio
4. Verificar dependencias previas
5. Ejecutar dentro del alcance
6. Ejecutar validaciones
7. Si falla, clasificar fallo
8. Reintentar solo si es seguro
9. Reportar estado final
10. No continuar con dependientes si hay Blocked o Needs Revision
```

---

## Decision de Supervision

El supervisor humano conserva la decision final cuando:

```text
- hay tension entre documentos
- hay cambio de arquitectura
- hay cambio de negocio
- hay cambio de contrato externo
- hay excepcion al orden de tasks
- hay que aceptar una task con gaps conocidos
```

El agente puede proponer. El supervisor decide.
