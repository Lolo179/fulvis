package com.fulvis.application.order.get;

import com.fulvis.application.order.OrderNotFoundException;
import com.fulvis.application.order.port.OrderRepositoryPort;
import com.fulvis.domain.order.Order;
import com.fulvis.domain.order.OrderEvent;
import com.fulvis.domain.order.OrderLine;
import com.fulvis.domain.order.OrderStateMachine;
import com.fulvis.domain.order.OrderStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetOrderUseCaseTest {

    private static final Instant CREATED_AT = Instant.parse("2026-08-26T09:00:00Z");
    private static final Instant TRANSITION_AT = Instant.parse("2026-08-26T09:05:00Z");

    @Test
    void givenExistingOrderWhenGettingOrderThenOrderSnapshotIsReturnedWithoutMutation() {
        Order order = inPreparationOrder();
        int historySize = order.history().size();
        FakeOrderRepository orderRepository = new FakeOrderRepository(order);
        GetOrderUseCase useCase = new GetOrderUseCase(orderRepository);

        GetOrderResult result = useCase.execute("order-123");

        assertThat(result.orderId()).isEqualTo("order-123");
        assertThat(result.status()).isEqualTo(OrderStatus.IN_PREPARATION);
        assertThat(result.createdAt()).isEqualTo(CREATED_AT);
        assertThat(result.updatedAt()).isEqualTo(TRANSITION_AT);
        assertThat(result.lines()).containsExactly(new GetOrderLineResult("item-123", 2));
        assertThat(result.history()).containsExactly(
                new GetOrderHistoryResult(null, OrderStatus.MANAGED, CREATED_AT),
                new GetOrderHistoryResult(OrderStatus.MANAGED, OrderStatus.IN_PREPARATION, TRANSITION_AT)
        );
        assertThat(order.status()).isEqualTo(OrderStatus.IN_PREPARATION);
        assertThat(order.history()).hasSize(historySize);
        assertThat(orderRepository.savedOrders).isEmpty();
    }

    @Test
    void givenMissingOrderWhenGettingOrderThenItFails() {
        FakeOrderRepository orderRepository = new FakeOrderRepository();
        GetOrderUseCase useCase = new GetOrderUseCase(orderRepository);

        assertThatThrownBy(() -> useCase.execute("missing-order"))
                .isInstanceOf(OrderNotFoundException.class);

        assertThat(orderRepository.savedOrders).isEmpty();
    }

    @Test
    void givenBlankOrderIdWhenGettingOrderThenItFailsBeforeRepositoryAccess() {
        FakeOrderRepository orderRepository = new FakeOrderRepository();
        GetOrderUseCase useCase = new GetOrderUseCase(orderRepository);

        assertThatThrownBy(() -> useCase.execute(" "))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(orderRepository.findByIdCalls).isZero();
    }

    private static Order inPreparationOrder() {
        Order order = Order.create("order-123", List.of(new OrderLine("item-123", 2)), CREATED_AT);
        order.applyTransition(
                new OrderStateMachine().resolve(OrderStatus.MANAGED, OrderEvent.START_PREPARATION).rule(),
                TRANSITION_AT
        );
        return order;
    }

    private static class FakeOrderRepository implements OrderRepositoryPort {

        private final Map<String, Order> ordersById = new LinkedHashMap<>();
        private final List<Order> savedOrders = new java.util.ArrayList<>();
        private int findByIdCalls;

        FakeOrderRepository(Order... orders) {
            for (Order order : orders) {
                ordersById.put(order.id(), order);
            }
        }

        @Override
        public Optional<Order> findById(String orderId) {
            findByIdCalls++;
            return Optional.ofNullable(ordersById.get(orderId));
        }

        @Override
        public void save(Order order) {
            savedOrders.add(order);
        }
    }
}
