package com.fulvis.domain.order;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final Instant CREATED_AT = Instant.parse("2026-08-20T10:00:00Z");
    private static final Instant OCCURRED_AT = Instant.parse("2026-08-20T10:05:00Z");

    @Test
    void givenManagedOrderWhenApplyingValidRuleThenStatusAndHistoryAreUpdated() {
        Order order = Order.create("order-123", List.of(new OrderLine("item-123", 2)), CREATED_AT);
        OrderTransitionRule rule = new OrderTransitionRule(
                OrderStatus.MANAGED,
                OrderEvent.START_PREPARATION,
                OrderStatus.IN_PREPARATION,
                StockAction.NONE
        );

        order.applyTransition(rule, OCCURRED_AT);

        assertThat(order.status()).isEqualTo(OrderStatus.IN_PREPARATION);
        assertThat(order.updatedAt()).isEqualTo(OCCURRED_AT);
        assertThat(order.history()).hasSize(2);
        assertThat(order.history().get(1))
                .isEqualTo(new OrderHistory(OrderStatus.MANAGED, OrderStatus.IN_PREPARATION, OCCURRED_AT));
    }

    @Test
    void givenOrderWhenCreatedThenInitialHistoryIsRegistered() {
        Order order = Order.create("order-123", List.of(new OrderLine("item-123", 2)), CREATED_AT);

        assertThat(order.status()).isEqualTo(OrderStatus.MANAGED);
        assertThat(order.history()).containsExactly(new OrderHistory(null, OrderStatus.MANAGED, CREATED_AT));
    }

    @Test
    void givenRuleFromDifferentStatusWhenEnsuringTransitionThenItFailsWithoutMutation() {
        Order order = Order.create("order-123", List.of(new OrderLine("item-123", 2)), CREATED_AT);
        OrderTransitionRule rule = new OrderTransitionRule(
                OrderStatus.IN_PREPARATION,
                OrderEvent.MARK_SHIPPED,
                OrderStatus.SHIPPED,
                StockAction.DECREMENT
        );

        assertThatThrownBy(() -> order.ensureCanApply(rule))
                .isInstanceOf(InvalidOrderTransitionException.class);

        assertThat(order.status()).isEqualTo(OrderStatus.MANAGED);
        assertThat(order.history()).hasSize(1);
    }

    @Test
    void givenDuplicatedItemLinesWhenCreatingOrderThenItFails() {
        List<OrderLine> lines = List.of(
                new OrderLine("item-123", 1),
                new OrderLine("item-123", 2)
        );

        assertThatThrownBy(() -> Order.create("order-123", lines, CREATED_AT))
                .isInstanceOf(InvalidOrderLinesException.class);
    }

    @Test
    void givenInvalidLineQuantityWhenCreatingOrderLineThenItFails() {
        assertThatThrownBy(() -> new OrderLine("item-123", 0))
                .isInstanceOf(InvalidOrderLinesException.class);
    }

    @Test
    void givenOrderCreatedEventWhenCreatingTransitionRuleThenItFails() {
        assertThatThrownBy(() -> new OrderTransitionRule(
                OrderStatus.MANAGED,
                OrderEvent.ORDER_CREATED,
                OrderStatus.IN_PREPARATION,
                StockAction.NONE
        )).isInstanceOf(InvalidOrderTransitionException.class);
    }

    @Test
    void givenSameSourceAndTargetStatusWhenCreatingTransitionRuleThenItFails() {
        assertThatThrownBy(() -> new OrderTransitionRule(
                OrderStatus.MANAGED,
                OrderEvent.START_PREPARATION,
                OrderStatus.MANAGED,
                StockAction.NONE
        )).isInstanceOf(InvalidOrderTransitionException.class);
    }
}
