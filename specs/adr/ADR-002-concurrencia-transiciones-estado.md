# ADR-002 - Control de concurrencia para transiciones de estado del pedido

**Estado:** Aceptado  
**Fecha:** 2026-08-14  
**Autor:** Luis M.

---

## Contexto

El pedido tiene un ciclo de vida con transiciones explicitamente validas e invalidas. La entidad `Order` es la responsable de hacer cumplir sus propias invariantes de estado: ningun componente externo puede cambiar el estado del pedido sin pasar por la logica de dominio de la entidad.

Las transiciones deben ser seguras ante peticiones concurrentes y ante reintentos legitimos originados por fallos de red o timeouts.

---

## Problema

Sin control de concurrencia, dos peticiones simultaneas sobre el mismo pedido pueden:

1. Ambas cargar la entidad con el mismo estado
2. Ambas pasar la validacion de dominio
3. Ambas aplicar la misma transicion, produciendo efectos duplicados o estados inconsistentes

Adicionalmente, un reintento legitimo de una operacion ya exitosa no debe ser tratado como un error.

---

## Alternativas descartadas

| Alternativa | Motivo de descarte |
|---|---|
| Locking pesimista (`SELECT FOR UPDATE`) | Contencion extremadamente baja sobre un pedido concreto; bloquear preventivamente es innecesario y penaliza el caso comun |
| Sin control de concurrencia | Produce transiciones duplicadas y estados inconsistentes |

---

## Decision

**Locking optimista** mediante campo `version` en la entidad `Order`, combinado con logica de idempotencia post-conflicto.

---

## Regla de idempotencia

Cuando el locking optimista detecta un conflicto (0 filas actualizadas al intentar escribir):

1. El sistema relee el estado actual del pedido
2. Si el estado actual **coincide con el estado destino** de la operacion -> la operacion se considera satisfecha, se devuelve exito
3. Si el estado actual **no permite** la operacion -> se lanza excepcion de dominio

Esto garantiza que reintentos de operaciones ya exitosas no produzcan falsos negativos.

---

## Justificacion

- Las operaciones sobre un pedido concreto tienen contencion extremadamente baja: es excepcional que dos actores operen simultaneamente sobre el mismo pedido.
- El locking optimista detecta el conflicto en el momento de escribir sin penalizar el caso comun (sin bloqueos en lectura).
- La regla de idempotencia protege contra fallos de red y reintentos sin anadir infraestructura adicional.

---

## Consecuencias

- Las transiciones de estado no bloquean otras lecturas ni escrituras concurrentes.
- Los conflictos genuinos producen excepciones de dominio con semantica clara.
- Las operaciones de transicion de estado son seguras de reintentar.
- Requiere un campo `version` en la entidad `Order` gestionado por el ORM.

---

## Relacion con otras decisiones

- ADR-001 establece locking pesimista para la reserva de stock, donde la contencion es mas alta y el contrato mas simple justifica el bloqueo.
- La eleccion entre optimista y pesimista debe revisarse caso a caso segun el perfil de contencion del recurso.
