package com.fulvis.application.order.transition;

import com.fulvis.domain.order.OrderEvent;

import java.util.Objects;

public record TransitionOrderCommand(String orderId, OrderEvent event) {

    public TransitionOrderCommand {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank");
        }
        Objects.requireNonNull(event, "event must not be null");
    }
}
