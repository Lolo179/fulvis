package com.fulvis.application.order.transition;

import com.fulvis.application.order.ItemNotFoundException;
import com.fulvis.application.order.OrderNotFoundException;
import com.fulvis.application.order.port.ItemRepositoryPort;
import com.fulvis.application.order.port.OrderRepositoryPort;
import com.fulvis.domain.item.Item;
import com.fulvis.domain.order.InvalidOrderTransitionException;
import com.fulvis.domain.order.Order;
import com.fulvis.domain.order.OrderEvent;
import com.fulvis.domain.order.OrderHistory;
import com.fulvis.domain.order.OrderLine;
import com.fulvis.domain.order.OrderStateMachine;
import com.fulvis.domain.order.OrderStatus;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransitionOrderUseCaseTest {

    private static final Instant CREATED_AT = Instant.parse("2026-08-26T09:00:00Z");
    private static final Instant NOW = Instant.parse("2026-08-26T09:15:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    void givenManagedOrderWhenStartPreparationThenOrderTransitionsWithoutTouchingStock() {
        Order order = managedOrder();
        FakeOrderRepository orderRepository = new FakeOrderRepository(order);
        FakeItemRepository itemRepository = new FakeItemRepository(new Item("item-123", "REF-123", "Fulvis item", 10, 2));
        TransitionOrderUseCase useCase = newUseCase(orderRepository, itemRepository);

        TransitionOrderResult result = useCase.execute(new TransitionOrderCommand("order-123", OrderEvent.START_PREPARATION));

        assertThat(result.orderId()).isEqualTo("order-123");
        assertThat(result.status()).isEqualTo(OrderStatus.IN_PREPARATION);
        assertThat(result.occurredAt()).isEqualTo(NOW);
        assertThat(result.idempotent()).isFalse();
        assertThat(order.status()).isEqualTo(OrderStatus.IN_PREPARATION);
        assertThat(order.history()).hasSize(2);
        assertThat(order.history().get(1)).isEqualTo(new OrderHistory(OrderStatus.MANAGED, OrderStatus.IN_PREPARATION, NOW));
        assertThat(orderRepository.savedOrders).containsExactly(order);
        assertThat(itemRepository.findAllCalls).isZero();
        assertThat(itemRepository.savedItems).isEmpty();
        assertThat(itemRepository.item("item-123").stockReserved()).isEqualTo(2);
    }

    @Test
    void givenInPreparationOrderWhenMarkShippedThenReservedStockIsDecrementedBeforeOrderIsSaved() {
        Order order = inPreparationOrder();
        FakeOrderRepository orderRepository = new FakeOrderRepository(order);
        FakeItemRepository itemRepository = new FakeItemRepository(new Item("item-123", "REF-123", "Fulvis item", 10, 2));
        TransitionOrderUseCase useCase = newUseCase(orderRepository, itemRepository);

        TransitionOrderResult result = useCase.execute(new TransitionOrderCommand("order-123", OrderEvent.MARK_SHIPPED));

        assertThat(result.status()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(result.idempotent()).isFalse();
        assertThat(itemRepository.item("item-123").stockTotal()).isEqualTo(8);
        assertThat(itemRepository.item("item-123").stockReserved()).isEqualTo(0);
        assertThat(order.status()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(order.history().get(2)).isEqualTo(new OrderHistory(OrderStatus.IN_PREPARATION, OrderStatus.SHIPPED, NOW));
        assertThat(orderRepository.savedOrders).containsExactly(order);
        assertThat(itemRepository.savedItems).containsExactly(itemRepository.item("item-123"));
    }

    @Test
    void givenManagedOrderWhenCancelThenReservedStockIsReleased() {
        Order order = managedOrder();
        FakeOrderRepository orderRepository = new FakeOrderRepository(order);
        FakeItemRepository itemRepository = new FakeItemRepository(new Item("item-123", "REF-123", "Fulvis item", 10, 2));
        TransitionOrderUseCase useCase = newUseCase(orderRepository, itemRepository);

        TransitionOrderResult result = useCase.execute(new TransitionOrderCommand("order-123", OrderEvent.CANCEL));

        assertThat(result.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(result.idempotent()).isFalse();
        assertThat(itemRepository.item("item-123").stockReserved()).isEqualTo(0);
        assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.history().get(1)).isEqualTo(new OrderHistory(OrderStatus.MANAGED, OrderStatus.CANCELLED, NOW));
        assertThat(orderRepository.savedOrders).containsExactly(order);
        assertThat(itemRepository.savedItems).containsExactly(itemRepository.item("item-123"));
    }

    @Test
    void givenAlreadySatisfiedOrderWhenRetriedThenItDoesNotTouchStockOrCreateHistory() {
        Order order = inPreparationOrder();
        int historySize = order.history().size();
        FakeOrderRepository orderRepository = new FakeOrderRepository(order);
        FakeItemRepository itemRepository = new FakeItemRepository(new Item("item-123", "REF-123", "Fulvis item", 10, 2));
        TransitionOrderUseCase useCase = newUseCase(orderRepository, itemRepository);

        TransitionOrderResult result = useCase.execute(new TransitionOrderCommand("order-123", OrderEvent.START_PREPARATION));

        assertThat(result.status()).isEqualTo(OrderStatus.IN_PREPARATION);
        assertThat(result.idempotent()).isTrue();
        assertThat(order.history()).hasSize(historySize);
        assertThat(orderRepository.savedOrders).isEmpty();
        assertThat(itemRepository.findAllCalls).isZero();
        assertThat(itemRepository.savedItems).isEmpty();
        assertThat(itemRepository.item("item-123").stockReserved()).isEqualTo(2);
    }

    @Test
    void givenInvalidTransitionWhenExecutedThenItDoesNotTouchStockOrCreateHistory() {
        Order order = shippedOrder();
        int historySize = order.history().size();
        FakeOrderRepository orderRepository = new FakeOrderRepository(order);
        FakeItemRepository itemRepository = new FakeItemRepository(new Item("item-123", "REF-123", "Fulvis item", 8, 0));
        TransitionOrderUseCase useCase = newUseCase(orderRepository, itemRepository);

        assertThatThrownBy(() -> useCase.execute(new TransitionOrderCommand("order-123", OrderEvent.CANCEL)))
                .isInstanceOf(InvalidOrderTransitionException.class);

        assertThat(order.status()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(order.history()).hasSize(historySize);
        assertThat(orderRepository.savedOrders).isEmpty();
        assertThat(itemRepository.findAllCalls).isZero();
        assertThat(itemRepository.savedItems).isEmpty();
    }

    @Test
    void givenLateCommandWhenExecutedThenItIsInvalidNotIdempotent() {
        Order order = shippedOrder();
        int historySize = order.history().size();
        FakeOrderRepository orderRepository = new FakeOrderRepository(order);
        FakeItemRepository itemRepository = new FakeItemRepository(new Item("item-123", "REF-123", "Fulvis item", 8, 0));
        TransitionOrderUseCase useCase = newUseCase(orderRepository, itemRepository);

        assertThatThrownBy(() -> useCase.execute(new TransitionOrderCommand("order-123", OrderEvent.START_PREPARATION)))
                .isInstanceOf(InvalidOrderTransitionException.class);

        assertThat(order.status()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(order.history()).hasSize(historySize);
        assertThat(orderRepository.savedOrders).isEmpty();
        assertThat(itemRepository.findAllCalls).isZero();
    }

    @Test
    void givenMissingOrderWhenExecutedThenItFailsBeforeResolvingStateMachineOrTouchingStock() {
        FakeOrderRepository orderRepository = new FakeOrderRepository();
        FakeItemRepository itemRepository = new FakeItemRepository(new Item("item-123", "REF-123", "Fulvis item", 10, 2));
        TransitionOrderUseCase useCase = newUseCase(orderRepository, itemRepository);

        assertThatThrownBy(() -> useCase.execute(new TransitionOrderCommand("missing-order", OrderEvent.CANCEL)))
                .isInstanceOf(OrderNotFoundException.class);

        assertThat(itemRepository.findAllCalls).isZero();
        assertThat(orderRepository.savedOrders).isEmpty();
    }

    @Test
    void givenMissingItemForStockActionWhenExecutedThenOrderDoesNotChange() {
        Order order = inPreparationOrder();
        int historySize = order.history().size();
        FakeOrderRepository orderRepository = new FakeOrderRepository(order);
        FakeItemRepository itemRepository = new FakeItemRepository();
        TransitionOrderUseCase useCase = newUseCase(orderRepository, itemRepository);

        assertThatThrownBy(() -> useCase.execute(new TransitionOrderCommand("order-123", OrderEvent.MARK_SHIPPED)))
                .isInstanceOf(ItemNotFoundException.class);

        assertThat(order.status()).isEqualTo(OrderStatus.IN_PREPARATION);
        assertThat(order.history()).hasSize(historySize);
        assertThat(orderRepository.savedOrders).isEmpty();
        assertThat(itemRepository.savedItems).isEmpty();
    }

    private static TransitionOrderUseCase newUseCase(FakeOrderRepository orderRepository, FakeItemRepository itemRepository) {
        return new TransitionOrderUseCase(orderRepository, itemRepository, new OrderStateMachine(), CLOCK);
    }

    private static Order managedOrder() {
        return Order.create("order-123", List.of(new OrderLine("item-123", 2)), CREATED_AT);
    }

    private static Order inPreparationOrder() {
        Order order = managedOrder();
        order.applyTransition(
                new OrderStateMachine().resolve(OrderStatus.MANAGED, OrderEvent.START_PREPARATION).rule(),
                Instant.parse("2026-08-26T09:05:00Z")
        );
        return order;
    }

    private static Order shippedOrder() {
        Order order = inPreparationOrder();
        order.applyTransition(
                new OrderStateMachine().resolve(OrderStatus.IN_PREPARATION, OrderEvent.MARK_SHIPPED).rule(),
                Instant.parse("2026-08-26T09:10:00Z")
        );
        return order;
    }

    private static class FakeOrderRepository implements OrderRepositoryPort {

        private final Map<String, Order> ordersById = new LinkedHashMap<>();
        private final List<Order> savedOrders = new ArrayList<>();

        FakeOrderRepository(Order... orders) {
            for (Order order : orders) {
                ordersById.put(order.id(), order);
            }
        }

        @Override
        public Optional<Order> findById(String orderId) {
            return Optional.ofNullable(ordersById.get(orderId));
        }

        @Override
        public void save(Order order) {
            savedOrders.add(order);
        }
    }

    private static class FakeItemRepository implements ItemRepositoryPort {

        private final Map<String, Item> itemsById = new LinkedHashMap<>();
        private final List<Item> savedItems = new ArrayList<>();
        private int findAllCalls;

        FakeItemRepository(Item... items) {
            for (Item item : items) {
                itemsById.put(item.id(), item);
            }
        }

        @Override
        public List<Item> findAllByIdsForUpdate(Collection<String> itemIds) {
            findAllCalls++;
            return itemIds.stream()
                    .filter(itemsById::containsKey)
                    .map(itemsById::get)
                    .toList();
        }

        @Override
        public void saveAll(Collection<Item> items) {
            savedItems.addAll(items);
        }

        Item item(String itemId) {
            return itemsById.get(itemId);
        }
    }
}
