package com.fulvis.adapters.out.persistence.order;

import com.fulvis.domain.order.OrderLine;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_lines")
public class OrderLineJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_line_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderJpaEntity order;

    @Column(name = "item_id", nullable = false)
    private String itemId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    protected OrderLineJpaEntity() {
    }

    private OrderLineJpaEntity(OrderJpaEntity order, OrderLine line) {
        this.order = order;
        this.itemId = line.itemId();
        this.quantity = line.quantity();
    }

    public static OrderLineJpaEntity from(OrderJpaEntity order, OrderLine line) {
        return new OrderLineJpaEntity(order, line);
    }

    public OrderLine toDomain() {
        return new OrderLine(itemId, quantity);
    }
}
