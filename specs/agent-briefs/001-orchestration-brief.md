# Orchestration Brief 001 - Migrate Docs to Specs Structure

**Estado:** Fase 1  
**Ultima revision:** 2026-08-19

---

## Proposito

Coordinar la ejecucion multiagente de:

```text
specs/agent-tasks/001-migrate-docs-to-specs-structure.md
```

Modalidad:

```text
Executor + Reviewer
```

---

## Roles

```text
Supervisor      -> Luis
Harness mentor  -> Codex durante la preparacion
Executor Agent  -> agente que ejecuta la migracion
Reviewer Agent  -> agente que revisa el resultado
```

---

## Orden De Ejecucion

```text
1. Supervisor confirma que se ejecuta Task 001
2. Executor recibe executor-agent.md + Task 001
3. Executor ejecuta la migracion
4. Executor entrega run report
5. Reviewer recibe reviewer-agent.md + Task 001 + run report + diff
6. Reviewer entrega review report
7. Supervisor decide Accepted, Needs Revision o Blocked
```

---

## Entradas Para Executor

```text
specs/agent-profiles/executor-agent.md
specs/agent-tasks/001-migrate-docs-to-specs-structure.md
contexto obligatorio listado en la task
```

---

## Salida Esperada Del Executor

```text
Run report compatible con AGENT_RUN_REPORT_TEMPLATE.md
Lista de archivos movidos
Lista de referencias actualizadas
Validaciones ejecutadas
Gaps y tensiones si existen
Estado final propuesto
```

---

## Entradas Para Reviewer

```text
specs/agent-profiles/reviewer-agent.md
specs/agent-tasks/001-migrate-docs-to-specs-structure.md
run report del executor
diff o lista de archivos modificados
```

---

## Salida Esperada Del Reviewer

```text
Review report
Hallazgos ordenados por severidad
Checks ejecutados
Estado final propuesto
Recomendacion para supervisor
```

---

## Puntos De Parada

Detener y escalar al supervisor si:

```text
- la task ya no esta Ready
- faltan documentos obligatorios
- mover docs/ genera contradiccion con ADR-004
- se necesita cambiar negocio, arquitectura o API
- aparecen referencias ambiguas que no pueden resolverse mecanicamente
- se requiere borrar informacion no cubierta por la task
```

---

## Decision De Integracion

El executor no acepta la task.

El reviewer tampoco integra el resultado.

Solo el supervisor puede decidir:

```text
Accepted
Needs Revision
Blocked
```
