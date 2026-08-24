package com.fulvis.domain.item;

public class Item {

    private final String id;
    private final String reference;
    private final String name;
    private int stockTotal;
    private int stockReserved;

    public Item(String id, String reference, String name, int stockTotal, int stockReserved) {
        this.id = requireText(id, "id");
        this.reference = requireText(reference, "reference");
        this.name = requireText(name, "name");
        ensureValidStock(stockTotal, stockReserved);
        this.stockTotal = stockTotal;
        this.stockReserved = stockReserved;
    }

    public void reserve(int quantity) {
        ensurePositiveQuantity(quantity);
        if (stockAvailable() < quantity) {
            throw new InsufficientStockException("Cannot reserve more than available stock");
        }
        stockReserved += quantity;
    }

    public void release(int quantity) {
        ensurePositiveQuantity(quantity);
        if (stockReserved < quantity) {
            throw new InsufficientStockException("Cannot release more than reserved stock");
        }
        stockReserved -= quantity;
    }

    public void decrement(int quantity) {
        ensurePositiveQuantity(quantity);
        if (stockReserved < quantity) {
            throw new InsufficientStockException("Cannot decrement more than reserved stock");
        }
        if (stockTotal < quantity) {
            throw new InsufficientStockException("Cannot decrement more than total stock");
        }
        stockTotal -= quantity;
        stockReserved -= quantity;
    }

    public String id() {
        return id;
    }

    public String reference() {
        return reference;
    }

    public String name() {
        return name;
    }

    public int stockTotal() {
        return stockTotal;
    }

    public int stockReserved() {
        return stockReserved;
    }

    public int stockAvailable() {
        return stockTotal - stockReserved;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static void ensureValidStock(int stockTotal, int stockReserved) {
        if (stockTotal < 0) {
            throw new IllegalArgumentException("stockTotal must not be negative");
        }
        if (stockReserved < 0) {
            throw new IllegalArgumentException("stockReserved must not be negative");
        }
        if (stockTotal < stockReserved) {
            throw new IllegalArgumentException("stockTotal must be greater than or equal to stockReserved");
        }
    }

    private static void ensurePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }
}
