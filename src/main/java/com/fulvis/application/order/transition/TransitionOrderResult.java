package com.fulvis.application.order.transition;

import com.fulvis.domain.order.OrderStatus;

import java.time.Instant;
import java.util.Objects;

public record TransitionOrderResult(String orderId, OrderStatus status, Instant occurredAt, boolean idempotent) {

    public TransitionOrderResult {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank");
        }
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }
}
