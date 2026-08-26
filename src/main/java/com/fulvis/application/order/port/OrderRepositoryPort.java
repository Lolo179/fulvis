package com.fulvis.application.order.port;

import com.fulvis.domain.order.Order;

import java.util.Optional;

public interface OrderRepositoryPort {

    Optional<Order> findById(String orderId);

    void save(Order order);
}
