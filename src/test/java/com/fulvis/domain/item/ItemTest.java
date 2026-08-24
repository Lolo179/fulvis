package com.fulvis.domain.item;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemTest {

    @Test
    void givenItemWhenReadingStockAvailableThenItIsDerivedFromTotalAndReserved() {
        Item item = new Item("item-123", "REF-123", "Fulvis item", 10, 3);

        assertThat(item.stockAvailable()).isEqualTo(7);
    }

    @Test
    void givenAvailableStockWhenReservingThenReservedStockIncreases() {
        Item item = new Item("item-123", "REF-123", "Fulvis item", 10, 2);

        item.reserve(3);

        assertThat(item.stockTotal()).isEqualTo(10);
        assertThat(item.stockReserved()).isEqualTo(5);
        assertThat(item.stockAvailable()).isEqualTo(5);
    }

    @Test
    void givenInsufficientAvailableStockWhenReservingThenItFailsWithoutMutation() {
        Item item = new Item("item-123", "REF-123", "Fulvis item", 4, 2);

        assertThatThrownBy(() -> item.reserve(3))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(item.stockTotal()).isEqualTo(4);
        assertThat(item.stockReserved()).isEqualTo(2);
    }

    @Test
    void givenReservedStockWhenReleasingThenReservedStockDecreases() {
        Item item = new Item("item-123", "REF-123", "Fulvis item", 10, 5);

        item.release(3);

        assertThat(item.stockTotal()).isEqualTo(10);
        assertThat(item.stockReserved()).isEqualTo(2);
        assertThat(item.stockAvailable()).isEqualTo(8);
    }

    @Test
    void givenInsufficientReservedStockWhenReleasingThenItFailsWithoutMutation() {
        Item item = new Item("item-123", "REF-123", "Fulvis item", 10, 2);

        assertThatThrownBy(() -> item.release(3))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(item.stockTotal()).isEqualTo(10);
        assertThat(item.stockReserved()).isEqualTo(2);
    }

    @Test
    void givenReservedStockWhenDecrementingThenTotalAndReservedStockDecrease() {
        Item item = new Item("item-123", "REF-123", "Fulvis item", 10, 2);

        item.decrement(2);

        assertThat(item.stockTotal()).isEqualTo(8);
        assertThat(item.stockReserved()).isEqualTo(0);
        assertThat(item.stockAvailable()).isEqualTo(8);
    }

    @Test
    void givenInsufficientReservedStockWhenDecrementingThenItFailsWithoutMutation() {
        Item item = new Item("item-123", "REF-123", "Fulvis item", 10, 1);

        assertThatThrownBy(() -> item.decrement(2))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(item.stockTotal()).isEqualTo(10);
        assertThat(item.stockReserved()).isEqualTo(1);
    }

    @Test
    void givenNegativeTotalStockWhenCreatingItemThenItFails() {
        assertThatThrownBy(() -> new Item("item-123", "REF-123", "Fulvis item", -1, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenReservedStockGreaterThanTotalWhenCreatingItemThenItFails() {
        assertThatThrownBy(() -> new Item("item-123", "REF-123", "Fulvis item", 1, 2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenInvalidQuantityWhenChangingStockThenItFails() {
        Item item = new Item("item-123", "REF-123", "Fulvis item", 10, 2);

        assertThatThrownBy(() -> item.reserve(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> item.release(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> item.decrement(0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
