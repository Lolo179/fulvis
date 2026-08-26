package com.fulvis.application.order.get;

import com.fulvis.domain.order.OrderStatus;

import java.time.Instant;
import java.util.Objects;

public record GetOrderHistoryResult(OrderStatus previousStatus, OrderStatus newStatus, Instant timestamp) {

    public GetOrderHistoryResult {
        Objects.requireNonNull(newStatus, "newStatus must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
    }
}
