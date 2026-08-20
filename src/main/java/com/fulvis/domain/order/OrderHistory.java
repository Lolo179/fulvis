package com.fulvis.domain.order;

import java.time.Instant;
import java.util.Objects;

public record OrderHistory(OrderStatus previousStatus, OrderStatus newStatus, Instant timestamp) {

    public OrderHistory {
        Objects.requireNonNull(newStatus, "newStatus must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
    }
}
