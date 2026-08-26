package com.fulvis.application.item.stock;

import com.fulvis.application.order.ItemNotFoundException;
import com.fulvis.application.order.port.ItemRepositoryPort;
import com.fulvis.domain.item.Item;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetItemStockUseCaseTest {

    @Test
    void givenExistingItemWhenGettingStockThenCurrentStockIsReturnedWithoutMutation() {
        Item item = new Item("item-123", "REF-123", "Fulvis item", 10, 3);
        FakeItemRepository itemRepository = new FakeItemRepository(item);
        GetItemStockUseCase useCase = new GetItemStockUseCase(itemRepository);

        GetItemStockResult result = useCase.execute("item-123");

        assertThat(result.itemId()).isEqualTo("item-123");
        assertThat(result.stockTotal()).isEqualTo(10);
        assertThat(result.stockReserved()).isEqualTo(3);
        assertThat(result.stockAvailable()).isEqualTo(7);
        assertThat(item.stockTotal()).isEqualTo(10);
        assertThat(item.stockReserved()).isEqualTo(3);
        assertThat(itemRepository.findAllForUpdateCalls).isZero();
        assertThat(itemRepository.saveAllCalls).isZero();
    }

    @Test
    void givenMissingItemWhenGettingStockThenItFails() {
        FakeItemRepository itemRepository = new FakeItemRepository();
        GetItemStockUseCase useCase = new GetItemStockUseCase(itemRepository);

        assertThatThrownBy(() -> useCase.execute("missing-item"))
                .isInstanceOf(ItemNotFoundException.class);

        assertThat(itemRepository.findAllForUpdateCalls).isZero();
        assertThat(itemRepository.saveAllCalls).isZero();
    }

    @Test
    void givenBlankItemIdWhenGettingStockThenItFailsBeforeRepositoryAccess() {
        FakeItemRepository itemRepository = new FakeItemRepository();
        GetItemStockUseCase useCase = new GetItemStockUseCase(itemRepository);

        assertThatThrownBy(() -> useCase.execute(" "))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(itemRepository.findByIdCalls).isZero();
    }

    private static class FakeItemRepository implements ItemRepositoryPort {

        private final Map<String, Item> itemsById = new LinkedHashMap<>();
        private int findByIdCalls;
        private int findAllForUpdateCalls;
        private int saveAllCalls;

        FakeItemRepository(Item... items) {
            for (Item item : items) {
                itemsById.put(item.id(), item);
            }
        }

        @Override
        public Optional<Item> findById(String itemId) {
            findByIdCalls++;
            return Optional.ofNullable(itemsById.get(itemId));
        }

        @Override
        public List<Item> findAllByIdsForUpdate(Collection<String> itemIds) {
            findAllForUpdateCalls++;
            return itemIds.stream()
                    .filter(itemsById::containsKey)
                    .map(itemsById::get)
                    .toList();
        }

        @Override
        public void saveAll(Collection<Item> items) {
            saveAllCalls++;
        }
    }
}
