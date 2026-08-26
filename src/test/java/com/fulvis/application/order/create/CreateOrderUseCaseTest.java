package com.fulvis.application.order.create;

import com.fulvis.application.order.ItemNotFoundException;
import com.fulvis.application.order.port.ItemRepositoryPort;
import com.fulvis.application.order.port.OrderRepositoryPort;
import com.fulvis.domain.item.InsufficientStockException;
import com.fulvis.domain.item.Item;
import com.fulvis.domain.order.InvalidOrderLinesException;
import com.fulvis.domain.order.Order;
import com.fulvis.domain.order.OrderHistory;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateOrderUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-08-26T09:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    void givenSufficientStockWhenCreatingOrderThenStockIsReservedAndOrderIsSaved() {
        FakeOrderRepository orderRepository = new FakeOrderRepository();
        FakeItemRepository itemRepository = new FakeItemRepository(new Item("item-123", "REF-123", "Fulvis item", 10, 2));
        CreateOrderUseCase useCase = new CreateOrderUseCase(orderRepository, itemRepository, CLOCK);

        CreateOrderResult result = useCase.execute(new CreateOrderCommand(List.of(
                new CreateOrderLineCommand("item-123", 3)
        )));

        assertThat(result.orderId()).isNotBlank();
        assertThat(result.status()).isEqualTo(OrderStatus.MANAGED);
        assertThat(result.createdAt()).isEqualTo(NOW);
        assertThat(itemRepository.item("item-123").stockReserved()).isEqualTo(5);
        assertThat(orderRepository.savedOrders).hasSize(1);
        Order savedOrder = orderRepository.savedOrders.getFirst();
        assertThat(savedOrder.status()).isEqualTo(OrderStatus.MANAGED);
        assertThat(savedOrder.lines()).hasSize(1);
        assertThat(savedOrder.lines().getFirst().itemId()).isEqualTo("item-123");
        assertThat(savedOrder.lines().getFirst().quantity()).isEqualTo(3);
        assertThat(savedOrder.history()).containsExactly(new OrderHistory(null, OrderStatus.MANAGED, NOW));
        assertThat(itemRepository.savedItems).containsExactly(itemRepository.item("item-123"));
    }

    @Test
    void givenInsufficientStockWhenCreatingOrderThenItFailsWithoutSavingOrderOrItems() {
        FakeOrderRepository orderRepository = new FakeOrderRepository();
        FakeItemRepository itemRepository = new FakeItemRepository(new Item("item-123", "REF-123", "Fulvis item", 4, 2));
        CreateOrderUseCase useCase = new CreateOrderUseCase(orderRepository, itemRepository, CLOCK);

        assertThatThrownBy(() -> useCase.execute(new CreateOrderCommand(List.of(
                new CreateOrderLineCommand("item-123", 3)
        )))).isInstanceOf(InsufficientStockException.class);

        assertThat(itemRepository.item("item-123").stockReserved()).isEqualTo(2);
        assertThat(orderRepository.savedOrders).isEmpty();
        assertThat(itemRepository.savedItems).isEmpty();
    }

    @Test
    void givenDuplicatedLinesWhenCreatingOrderThenItFailsBeforeLoadingItems() {
        FakeOrderRepository orderRepository = new FakeOrderRepository();
        FakeItemRepository itemRepository = new FakeItemRepository(new Item("item-123", "REF-123", "Fulvis item", 10, 2));
        CreateOrderUseCase useCase = new CreateOrderUseCase(orderRepository, itemRepository, CLOCK);

        assertThatThrownBy(() -> useCase.execute(new CreateOrderCommand(List.of(
                new CreateOrderLineCommand("item-123", 1),
                new CreateOrderLineCommand("item-123", 2)
        )))).isInstanceOf(InvalidOrderLinesException.class);

        assertThat(itemRepository.findAllCalls).isZero();
        assertThat(orderRepository.savedOrders).isEmpty();
        assertThat(itemRepository.item("item-123").stockReserved()).isEqualTo(2);
    }

    @Test
    void givenInvalidQuantityWhenCreatingOrderThenItFailsBeforeLoadingItems() {
        FakeOrderRepository orderRepository = new FakeOrderRepository();
        FakeItemRepository itemRepository = new FakeItemRepository(new Item("item-123", "REF-123", "Fulvis item", 10, 2));
        CreateOrderUseCase useCase = new CreateOrderUseCase(orderRepository, itemRepository, CLOCK);

        assertThatThrownBy(() -> useCase.execute(new CreateOrderCommand(List.of(
                new CreateOrderLineCommand("item-123", 0)
        )))).isInstanceOf(InvalidOrderLinesException.class);

        assertThat(itemRepository.findAllCalls).isZero();
        assertThat(orderRepository.savedOrders).isEmpty();
        assertThat(itemRepository.item("item-123").stockReserved()).isEqualTo(2);
    }

    @Test
    void givenMissingItemWhenCreatingOrderThenItFailsWithoutSavingOrderOrItems() {
        FakeOrderRepository orderRepository = new FakeOrderRepository();
        FakeItemRepository itemRepository = new FakeItemRepository();
        CreateOrderUseCase useCase = new CreateOrderUseCase(orderRepository, itemRepository, CLOCK);

        assertThatThrownBy(() -> useCase.execute(new CreateOrderCommand(List.of(
                new CreateOrderLineCommand("missing-item", 1)
        )))).isInstanceOf(ItemNotFoundException.class);

        assertThat(itemRepository.findAllCalls).isOne();
        assertThat(orderRepository.savedOrders).isEmpty();
        assertThat(itemRepository.savedItems).isEmpty();
    }

    private static class FakeOrderRepository implements OrderRepositoryPort {

        private final List<Order> savedOrders = new ArrayList<>();

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
