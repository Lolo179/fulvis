package com.fulvis.domain.order;

public record OrderLine(String itemId, int quantity) {

    public OrderLine {
        if (itemId == null || itemId.isBlank()) {
            throw new InvalidOrderLinesException("itemId must not be blank");
        }
        if (quantity <= 0) {
            throw new InvalidOrderLinesException("quantity must be positive");
        }
    }
}
