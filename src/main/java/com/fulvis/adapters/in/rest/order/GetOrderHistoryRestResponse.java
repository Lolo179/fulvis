package com.fulvis.adapters.in.rest.order;

import com.fulvis.domain.order.OrderStatus;

import java.time.Instant;

public record GetOrderHistoryRestResponse(OrderStatus previousStatus, OrderStatus newStatus, Instant timestamp) {
}
