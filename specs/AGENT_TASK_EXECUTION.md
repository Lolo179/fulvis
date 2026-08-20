# AGENT_TASK_EXECUTION.md - Fulvis

**Estado:** Fase 1  
**Ultima revision:** 2026-08-19

---

## Proposito

Este documento define como debe ejecutarse una agent task cuando ya esta en estado `Ready`.

No decide si una task esta lista. Eso pertenece a `AGENT_READINESS_REVIEW.md`.

No define estados ni politica anti-cascada general. Eso pertenece a `AGENT_BACKLOG_GOVERNANCE.md`.

Este documento describe el protocolo operativo de ejecucion para humanos, agentes, harnesses, tools y skills.

---

## Principio

Una task `Ready` se ejecuta con autonomia limitada:

```text
El agente puede resolver problemas tecnicos dentro del alcance, pero no puede cambiar negocio, arquitectura, contratos o ADRs sin supervision.
```

---

## Flujo de Ejecucion

```text
1. Leer la task completa
2. Confirmar que esta en estado Ready
3. Leer contexto obligatorio
4. Comprobar dependencias previas
5. Revisar alcance permitido y fuera de alcance
6. Ejecutar cambios dentro del alcance
7. Ejecutar validaciones definidas
8. Clasificar errores si aparecen
9. Reintentar solo si es seguro
10. Reportar salida final
```

---

## Preflight

Antes de modificar archivos, el agente debe verificar:

```text
- la task existe
- la task esta Ready
- no hay dependencias previas en Blocked o Needs Revision
- los documentos de contexto obligatorio existen
- no hay tension evidente entre task, specs y ADRs
- el alcance esta claro
- las validaciones esperadas estan claras
```

Si falla el preflight:

```text
- no iniciar implementacion
- proponer Blocked o Needs Revision
- reportar motivo
```

---

## Lectura de Contexto

El agente debe leer:

```text
- todos los documentos indicados en Contexto Obligatorio
- specs aplicables
- ADRs aplicables
- quality gates aplicables
- documentos de governance si la task afecta agent tasks
```

El contexto debe prepararse segun `AGENT_CONTEXT_PACKS.md`.

Regla:

```text
No implementar antes de leer el contexto obligatorio.
```

---

## Control de Alcance

El agente solo puede modificar archivos permitidos por la task.

Si necesita tocar un archivo fuera de alcance:

```text
- detenerse
- reportar por que lo necesita
- proponer cambio de task o nueva task
- esperar decision del supervisor
```

Excepcion:

```text
Se permite actualizar SPEC_COVERAGE.md si la propia task lo autoriza y cambia cobertura.
```

---

## Cambios Permitidos Automaticamente

El agente puede corregir automaticamente:

```text
- imports
- typos
- formato
- nombres de archivos dentro del alcance
- tests rotos por errores mecanicos
- pequenas desalineaciones con convenciones ya documentadas
```

Siempre que:

```text
- no cambie reglas de negocio
- no cambie arquitectura
- no cambie API publica
- no cambie ADRs
- no amplie alcance
```

---

## Cambios Que Requieren Supervision

El agente no puede decidir automaticamente:

```text
- cambiar estados de Order
- cambiar eventos de Order
- cambiar acciones de stock
- cambiar endpoints REST
- cambiar mapeo de errores
- relajar quality gates
- modificar ADRs aceptadas
- cambiar estrategia de concurrencia
- cambiar limite transaccional
- introducir herramientas externas nuevas
- saltarse una task previa
```

Si aparece cualquiera de estos casos:

```text
Estado final propuesto: Blocked o Needs Revision
```

---

## Reintentos

El agente puede reintentar hasta:

```text
maxRetries = 2
```

Solo si:

```text
- el fallo es tecnico y local
- la causa probable esta identificada
- la correccion esta dentro del alcance
- el reintento no oculta el fallo original
```

Cada reintento debe quedar reportado.

---

## Validaciones

El agente debe ejecutar las validaciones definidas en la task.

Si no puede ejecutarlas:

```text
- explicar por que
- indicar riesgo
- proponer validacion alternativa si existe
```

Validaciones comunes:

```text
- mvn test
- mvn verify
- tests unitarios
- integration tests
- architecture checks
- OpenAPI validation
- busqueda de referencias rotas
- busqueda de caracteres no ASCII en docs
```

---

## Salida Final

Toda ejecucion debe terminar con:

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

La salida final debe poder trasladarse a `AGENT_RUN_REPORT_TEMPLATE.md`.

Si existe Git, el reporte debe indicar:

```text
- rama
- worktree
- commit base
- commit final
```

Si existen datos de uso, el reporte debe incluir resource accounting segun `AGENT_RESOURCE_ACCOUNTING.md`.

---

## Estados Finales Permitidos

Una ejecucion puede proponer:

```text
Accepted
Blocked
Needs Revision
```

No debe proponer `Accepted` si:

```text
- no cumple criterio de done
- no ejecuto validaciones criticas disponibles
- hay tension no resuelta
- hay cambios fuera de alcance
- hay gaps no declarados
```

---

## Relacion Con Otros Documentos

```text
AGENT_BACKLOG_GOVERNANCE.md -> estados, anti-cascada y recuperacion
AGENT_READINESS_REVIEW.md   -> promocion de Draft a Ready
AGENT_TASK_EXECUTION.md     -> ejecucion de una task Ready
AGENT_RUN_REPORT_TEMPLATE.md -> evidencia estructurada de una ejecucion
AGENT_CONTEXT_PACKS.md      -> construccion del contexto minimo suficiente
AGENT_RESOURCE_ACCOUNTING.md -> medicion de tokens, coste, tools y MCPs
AGENT_TASK_TEMPLATE.md      -> estructura estandar de tasks
QUALITY_GATES.md            -> restricciones globales de aceptacion
SPEC_COVERAGE.md            -> trazabilidad de cobertura
```

---

## Nota Para Harness

Un harness puede convertir este documento en una maquina de estados simple:

```text
Ready -> preflight -> execute -> validate -> report -> Accepted
Ready -> preflight failed -> Needs Revision / Blocked
Ready -> execution failed -> retry <= 2 -> report
Ready -> unsafe decision detected -> Blocked
```
