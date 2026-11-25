package com.fulvis.adapters.out.persistence.order;

import com.fulvis.application.error.ConcurrencyConflictException;
import com.fulvis.domain.order.Order;
import com.fulvis.domain.order.OrderHistory;
import com.fulvis.domain.order.OrderLine;
import com.fulvis.domain.order.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderJpaEntityTest {

    private static final Instant CREATED_AT = Instant.parse("2026-08-18T10:15:30Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-08-18T10:17:00Z");

    @Test
    void givenOrderJpaEntityWhenMappedToDomainThenItRestoresLinesHistoryAndVersion() {
        Order order = Order.rehydrate(
                "order-123",
                OrderStatus.IN_PREPARATION,
                CREATED_AT,
                UPDATED_AT,
                List.of(new OrderLine("item-123", 2)),
                List.of(
                        new OrderHistory(null, OrderStatus.MANAGED, CREATED_AT),
                        new OrderHistory(OrderStatus.MANAGED, OrderStatus.IN_PREPARATION, UPDATED_AT)
                ),
                4L
        );
        OrderJpaEntity entity = OrderJpaEntity.from(order);
        ReflectionTestUtils.setField(entity, "version", 4L);

        Order restored = entity.toDomain();

        assertThat(restored.id()).isEqualTo("order-123");
        assertThat(restored.status()).isEqualTo(OrderStatus.IN_PREPARATION);
        assertThat(restored.lines()).containsExactly(new OrderLine("item-123", 2));
        assertThat(restored.history()).containsExactly(
                new OrderHistory(null, OrderStatus.MANAGED, CREATED_AT),
                new OrderHistory(OrderStatus.MANAGED, OrderStatus.IN_PREPARATION, UPDATED_AT)
        );
        assertThat(restored.version()).isEqualTo(4L);
    }

    @Test
    void givenStaleOrderVersionWhenUpdatingEntityThenItFailsAsConcurrencyConflict() {
        Order current = Order.rehydrate(
                "order-123",
                OrderStatus.MANAGED,
                CREATED_AT,
                CREATED_AT,
                List.of(new OrderLine("item-123", 2)),
                List.of(new OrderHistory(null, OrderStatus.MANAGED, CREATED_AT)),
                4L
        );
        Order stale = Order.rehydrate(
                "order-123",
                OrderStatus.IN_PREPARATION,
                CREATED_AT,
                UPDATED_AT,
                List.of(new OrderLine("item-123", 2)),
                List.of(
                        new OrderHistory(null, OrderStatus.MANAGED, CREATED_AT),
                        new OrderHistory(OrderStatus.MANAGED, OrderStatus.IN_PREPARATION, UPDATED_AT)
                ),
                3L
        );
        OrderJpaEntity entity = OrderJpaEntity.from(current);
        ReflectionTestUtils.setField(entity, "version", 4L);

        assertThatThrownBy(() -> entity.updateFrom(stale))
                .isInstanceOf(ConcurrencyConflictException.class);
    }
}
