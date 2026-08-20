# AGENT_RUN_REPORT_TEMPLATE.md - Fulvis

**Estado:** Fase 1  
**Ultima revision:** 2026-08-19

---

## Proposito

Plantilla para registrar el resultado de una ejecucion de agent task.

Este reporte sirve como evidencia para supervision humana, harnesses, Pull Requests y futuras herramientas de auditoria.

No sustituye a la task. La task define que hacer. El run report registra que ocurrio al ejecutarla.

---

## Identificacion

```text
Run id:
Fecha:
Agente:
Task ejecutada:
Rama:
Worktree:
Commit base:
Commit final:
```

Notas:

```text
- Run id debe ser unico dentro del proyecto.
- Commit base y commit final pueden quedar vacios si aun no existe Git, pero deben reportarse como gap.
```

---

## Estado

```text
Estado inicial de la task:
Estado final propuesto:
```

Estados finales permitidos:

```text
Accepted
Blocked
Needs Revision
```

---

## Contexto

```text
Docs leidos:

Specs leidas:

ADRs leidas:

Quality gates revisados:
```

---

## Resumen Del Cambio

```text
Resumen:

Archivos modificados:

Archivos creados:

Archivos eliminados:
```

---

## Validaciones

```text
Comandos ejecutados:

Resultados:

Validaciones no ejecutadas:

Motivo de validaciones no ejecutadas:
```

Ejemplo:

```text
mvn test -> passed
mvn verify -> failed
rg -n "[^\x00-\x7F]" docs -> passed
```

---

## Reintentos

```text
Numero de reintentos:

Reintento 1:
- causa:
- cambio aplicado:
- resultado:

Reintento 2:
- causa:
- cambio aplicado:
- resultado:
```

Regla:

```text
No superar maxRetries = 2 salvo autorizacion explicita del supervisor.
```

---

## Tensiones y Gaps

```text
Tensiones detectadas:

Gaps restantes:

Riesgo de cascada:

Decision requerida del supervisor:
```

---

## Resource Accounting

```text
Model:
Input tokens:
Output tokens:
Cached input tokens:
Total tokens:
Estimated cost:
Tool calls:
MCP calls:
Context pack:
Documents loaded:
Retry cost:
Budget exceeded: yes/no
```

Si estos datos no estan disponibles:

```text
Resource accounting unavailable:
Motivo:
```

---

## Alcance

```text
Cambios dentro de alcance:

Cambios fuera de alcance:

Justificacion si hubo cambios fuera de alcance:
```

Regla:

```text
Si hubo cambios fuera de alcance no autorizados, el estado final propuesto no puede ser Accepted.
```

---

## Decision Propuesta

```text
Estado final propuesto:

Motivo:

Siguiente paso recomendado:
```

Estados permitidos:

```text
Accepted:
- cumple criterio de done
- validaciones criticas pasaron
- no quedan tensiones bloqueantes

Blocked:
- falta decision, dependencia externa o herramienta
- continuar generaria riesgo de cascada

Needs Revision:
- la task necesita ajuste
- hay ambiguedad, referencia rota, alcance incorrecto o criterio de done incompleto
```

---

## Checklist Final

```text
- La task fue leida completa
- La task estaba Ready antes de ejecutar
- Se leyo el contexto obligatorio
- Se respeto el alcance
- Se ejecutaron validaciones disponibles
- Se reportaron validaciones no ejecutadas
- Se reportaron reintentos
- Se reportaron tensiones
- Se reportaron gaps
- Se evaluo riesgo de cascada
```

---

## Formato Compacto Para PR

Cuando el run report se copie a un Pull Request, puede resumirse asi:

```text
Task:
Estado final propuesto:
Resumen:
Validaciones:
Reintentos:
Tensiones:
Gaps:
Riesgo de cascada:
```
