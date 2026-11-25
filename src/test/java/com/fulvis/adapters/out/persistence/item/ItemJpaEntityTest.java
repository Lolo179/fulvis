package com.fulvis.adapters.out.persistence.item;

import com.fulvis.domain.item.Item;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemJpaEntityTest {

    @Test
    void givenItemJpaEntityWhenMappedToDomainThenStockAvailableIsDerived() {
        Item item = new Item("item-123", "SKU-123", "Boots", 10, 3);

        Item restored = ItemJpaEntity.from(item).toDomain();

        assertThat(restored.stockTotal()).isEqualTo(10);
        assertThat(restored.stockReserved()).isEqualTo(3);
        assertThat(restored.stockAvailable()).isEqualTo(7);
    }
}
