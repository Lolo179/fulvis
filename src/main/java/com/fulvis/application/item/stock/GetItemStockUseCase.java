package com.fulvis.application.item.stock;

import com.fulvis.application.order.ItemNotFoundException;
import com.fulvis.application.order.port.ItemRepositoryPort;
import com.fulvis.domain.item.Item;

import java.util.Objects;

public class GetItemStockUseCase {

    private final ItemRepositoryPort itemRepository;

    public GetItemStockUseCase(ItemRepositoryPort itemRepository) {
        this.itemRepository = Objects.requireNonNull(itemRepository, "itemRepository must not be null");
    }

    public GetItemStockResult execute(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("itemId must not be blank");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));

        return new GetItemStockResult(
                item.id(),
                item.stockTotal(),
                item.stockReserved(),
                item.stockAvailable()
        );
    }
}
