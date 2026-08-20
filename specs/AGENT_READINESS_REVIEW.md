# AGENT_READINESS_REVIEW.md - Fulvis

**Estado:** Fase 1  
**Ultima revision:** 2026-08-19

---

## Proposito

Este documento define el proceso recurrente para revisar si una agent task puede pasar de `Draft` a `Ready`.

No es una task numerada de implementacion. Es un gate de supervision que puede ejecutarse cuando el supervisor quiera preparar la siguiente task para agentes.

---

## Principio

La readiness review no revisa eternamente el backlog ni busca perfeccion.

Hace una pasada finita para decidir:

```text
Que task puede ejecutarse ahora con seguridad?
```

---

## Contexto Obligatorio

Antes de hacer una readiness review, revisar:

```text
specs/CONVENTIONS.md
specs/AGENT_TASK_TEMPLATE.md
specs/AGENT_BACKLOG_GOVERNANCE.md
specs/QUALITY_GATES.md
specs/SPEC_COVERAGE.md
specs/architecture/agent-backlog-governance.spec.md
specs/architecture/version-control.spec.md
specs/agent-tasks/*
```

---

## Criterio Para Promover a Ready

Una task puede pasar de `Draft` a `Ready` si:

```text
- tiene objetivo claro
- tiene contexto obligatorio
- tiene fuentes de verdad aplicables
- tiene alcance claro
- tiene restricciones claras
- tiene specs aplicables
- tiene quality gates aplicables
- tiene criterio de done
- tiene output esperado del agente
- tiene politica de revision
- no depende implicitamente de una task posterior
- no contradice ADRs aceptadas
- su ejecucion no requiere decisiones pendientes
```

---

## Estrategia

No se debe activar todo el backlog a la vez.

Orden recomendado:

```text
1. Revisar la secuencia completa de agent tasks una vez
2. Detectar errores estructurales
3. Promover a Ready solo la siguiente task ejecutable
4. Si hay varias independientes, justificarlo explicitamente
5. Mantener el resto en Draft hasta que sus dependencias previas progresen
```

---

## Limite Anti-Bucle

Esta revision realiza una unica pasada sobre el backlog.

No debe:

```text
- iniciar una segunda pasada completa automaticamente
- corregir profundamente todas las tasks
- ejecutar tasks tecnicas
- perseguir perfeccion documental
- llamarse a si misma
- ejecutar tasks dependientes
```

Promociones maximas por ejecucion:

```text
1 task a Ready
```

Excepcion:

```text
El supervisor puede autorizar varias promociones si son independientes y el riesgo de cascada es bajo.
```

Si aparecen problemas amplios:

```text
- marcar la task afectada como Needs Revision o Blocked
- reportar el motivo
- terminar la revision
```

Cualquier nueva revision posterior debe ejecutarse como una nueva instancia del proceso, no como continuacion automatica.

---

## Checks Esperados

```text
- cada task tiene estado valido
- cada task tiene titulo numerado coherente con filename
- cada task tiene secciones minimas requeridas
- no hay referencias a agent tasks inexistentes
- no hay dependencias numericamente posteriores no justificadas
- las tasks Ready no tienen dependencias Draft obligatorias
- SPEC_COVERAGE.md no apunta a filenames inexistentes
```

---

## Resultado Esperado

Toda readiness review debe terminar reportando:

```text
Resumen:

Tasks revisadas:

Task promovida a Ready:

Tasks marcadas Needs Revision:

Tasks marcadas Blocked:

Validaciones ejecutadas:

Tensiones detectadas:

Gaps restantes:

Riesgo de cascada:
```

---

## Candidato Inicial

Tras la migracion documental, el candidato natural para `Ready` es:

```text
002-create-project-scaffold.md
```

La task `001-migrate-docs-to-specs-structure.md` debe revisarse y aceptarse antes de ejecutar `002-create-project-scaffold.md`.
