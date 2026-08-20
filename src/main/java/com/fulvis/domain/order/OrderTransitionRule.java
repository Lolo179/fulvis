package com.fulvis.domain.order;

import java.util.Objects;

public record OrderTransitionRule(OrderStatus from, OrderEvent event, OrderStatus to, StockAction requiredStockAction) {

    public OrderTransitionRule {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(event, "event must not be null");
        Objects.requireNonNull(to, "to must not be null");
        Objects.requireNonNull(requiredStockAction, "requiredStockAction must not be null");
        if (event == OrderEvent.ORDER_CREATED) {
            throw new InvalidOrderTransitionException("ORDER_CREATED is not an operational transition event");
        }
        if (from == to) {
            throw new InvalidOrderTransitionException("OrderTransitionRule must move the order to a different status");
        }
    }
}
