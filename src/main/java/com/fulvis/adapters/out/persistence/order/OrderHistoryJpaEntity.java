package com.fulvis.adapters.out.persistence.order;

import com.fulvis.domain.order.OrderHistory;
import com.fulvis.domain.order.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "order_history")
public class OrderHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderJpaEntity order;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private OrderStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private OrderStatus newStatus;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    protected OrderHistoryJpaEntity() {
    }

    private OrderHistoryJpaEntity(OrderJpaEntity order, OrderHistory history) {
        this.order = order;
        this.previousStatus = history.previousStatus();
        this.newStatus = history.newStatus();
        this.timestamp = history.timestamp();
    }

    public static OrderHistoryJpaEntity from(OrderJpaEntity order, OrderHistory history) {
        return new OrderHistoryJpaEntity(order, history);
    }

    public OrderHistory toDomain() {
        return new OrderHistory(previousStatus, newStatus, timestamp);
    }
}
