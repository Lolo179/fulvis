package com.fulvis.application.order.port;

import com.fulvis.domain.order.Order;

public interface OrderRepositoryPort {

    void save(Order order);
}
