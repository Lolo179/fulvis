package com.fulvis.adapters.out.persistence.order;

import com.fulvis.application.error.ConcurrencyConflictException;
import com.fulvis.domain.order.Order;
import com.fulvis.domain.order.OrderHistory;
import com.fulvis.domain.order.OrderLine;
import com.fulvis.domain.order.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id
    @Column(name = "order_id", nullable = false, updatable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @OrderBy("id ASC")
    private final List<OrderLineJpaEntity> lines = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @OrderBy("id ASC")
    private final List<OrderHistoryJpaEntity> history = new ArrayList<>();

    protected OrderJpaEntity() {
    }

    private OrderJpaEntity(Order order) {
        this.id = order.id();
        this.status = order.status();
        this.createdAt = order.createdAt();
        this.updatedAt = order.updatedAt();
        order.lines().stream()
                .map(line -> OrderLineJpaEntity.from(this, line))
                .forEach(lines::add);
        appendHistory(order.history());
    }

    public static OrderJpaEntity from(Order order) {
        return new OrderJpaEntity(order);
    }

    public void updateFrom(Order order) {
        if (!Objects.equals(version, order.version())) {
            throw new ConcurrencyConflictException("Order version conflict for orderId " + order.id());
        }

        this.status = order.status();
        this.updatedAt = order.updatedAt();
        appendHistory(order.history());
    }

    private void appendHistory(List<OrderHistory> entries) {
        entries.subList(history.size(), entries.size()).stream()
                .map(historyEntry -> OrderHistoryJpaEntity.from(this, historyEntry))
                .forEach(history::add);
    }

    public Order toDomain() {
        return Order.rehydrate(
                id,
                status,
                createdAt,
                updatedAt,
                lines.stream()
                        .map(OrderLineJpaEntity::toDomain)
                        .toList(),
                history.stream()
                        .map(OrderHistoryJpaEntity::toDomain)
                        .toList(),
                version
        );
    }
}
