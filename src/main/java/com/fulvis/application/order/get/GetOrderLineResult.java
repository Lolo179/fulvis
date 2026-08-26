package com.fulvis.application.order.get;

public record GetOrderLineResult(String itemId, int quantity) {

    public GetOrderLineResult {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("itemId must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }
}
