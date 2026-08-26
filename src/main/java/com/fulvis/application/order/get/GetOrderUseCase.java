package com.fulvis.application.order.get;

import com.fulvis.application.order.OrderNotFoundException;
import com.fulvis.application.order.port.OrderRepositoryPort;
import com.fulvis.domain.order.Order;

import java.util.Objects;

public class GetOrderUseCase {

    private final OrderRepositoryPort orderRepository;

    public GetOrderUseCase(OrderRepositoryPort orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository must not be null");
    }

    public GetOrderResult execute(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        return new GetOrderResult(
                order.id(),
                order.status(),
                order.createdAt(),
                order.updatedAt(),
                order.lines().stream()
                        .map(line -> new GetOrderLineResult(line.itemId(), line.quantity()))
                        .toList(),
                order.history().stream()
                        .map(history -> new GetOrderHistoryResult(history.previousStatus(), history.newStatus(), history.timestamp()))
                        .toList()
        );
    }
}
