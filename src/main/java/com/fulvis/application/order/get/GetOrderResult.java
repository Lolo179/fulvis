package com.fulvis.application.order.get;

import com.fulvis.domain.order.OrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record GetOrderResult(
        String orderId,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt,
        List<GetOrderLineResult> lines,
        List<GetOrderHistoryResult> history
) {

    public GetOrderResult {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank");
        }
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        lines = List.copyOf(Objects.requireNonNull(lines, "lines must not be null"));
        history = List.copyOf(Objects.requireNonNull(history, "history must not be null"));
    }
}
