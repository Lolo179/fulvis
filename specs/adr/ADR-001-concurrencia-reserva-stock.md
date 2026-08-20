# ADR-001 - Control de concurrencia para reserva de stock

**Estado:** Aceptado  
**Fecha:** 2026-08-12  
**Autor:** Luis M.

---

## Contexto

La creacion de un pedido en Fulvis requiere ejecutar la siguiente secuencia:

1. Leer el `stock_available` del producto solicitado
2. Validar que `stock_available >= requested_quantity`
3. Reservar las unidades: `stock_reserved += quantity`

Esta secuencia debe ser atomica e aislada de otras peticiones concurrentes que operen sobre el mismo producto.

---

## Problema

Sin control de concurrencia, dos peticiones simultaneas para el mismo producto pueden:

1. Ambas leer `stock_available = N`
2. Ambas pasar la validacion
3. Ambas ejecutar la reserva

Resultado: `stock_available` queda en negativo sin ningun error del sistema. La violacion de la regla de negocio ocurre silenciosamente.

Este problema se produce incluso con `@Transactional`, porque el nivel de aislamiento por defecto (`READ COMMITTED`) no bloquea la fila en el momento de la lectura.

---

## Alternativas descartadas

| Alternativa | Motivo de descarte |
|---|---|
| `@Transactional` solo | No bloquea la fila al leer; el conflicto ocurre antes del UPDATE |
| Bloqueo a nivel de aplicacion (lock por ID en memoria) | No funciona con multiples instancias del servicio |
| Cola de mensajeria (serializacion por evento) | Introduce asincronia innecesaria; cambia el contrato de la API sincrona |

---

## Decision

**Locking pesimista** mediante `SELECT ... FOR UPDATE` dentro de una transaccion de base de datos (`@Transactional`).

Al leer el registro de stock del producto, se adquiere un lock exclusivo sobre esa fila. Las peticiones concurrentes sobre el mismo producto quedan bloqueadas hasta que la transaccion activa hace commit o rollback.

---

## Justificacion

- Fase 1 tiene un perfil de **baja concurrencia**: API interna, numero limitado y controlado de operadores.
- El contrato resultante es simple y determinista: el llamante espera y recibe un resultado definitivo (exito o error de negocio). No requiere logica de reintento en el cliente.
- El coste de bloqueo (serializacion de peticiones) es negligible dado el volumen esperado.
- La implementacion es directa: no requiere infraestructura adicional ni cambios en el modelo de datos.

---

## Consecuencias

- Las peticiones concurrentes sobre el mismo producto se serializan en la base de datos.
- Bajo alta contencion (muchas peticiones simultaneas sobre el mismo producto), esta estrategia puede convertirse en un cuello de botella.
- No introduce logica de reintento ni gestion de conflictos en la capa de aplicacion.

---

## Revision prevista

Migrar a **locking optimista** (campo `version` en la entidad de stock) cuando se observe alguna de las siguientes condiciones:

- Metricas de contencion observables: lock wait timeouts o deadlocks registrados en logs
- Cambio en el perfil de uso: apertura de la API a actores externos o aumento significativo de operadores concurrentes

En ese escenario, la gestion de conflictos pasara a la capa de aplicacion, eliminando el bloqueo en base de datos a cambio de logica de deteccion y reintento.
