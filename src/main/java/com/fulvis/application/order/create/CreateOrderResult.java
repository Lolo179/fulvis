package com.fulvis.application.order.create;

import com.fulvis.domain.order.OrderStatus;

import java.time.Instant;
import java.util.Objects;

public record CreateOrderResult(String orderId, OrderStatus status, Instant createdAt) {

    public CreateOrderResult {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank");
        }
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }
}
