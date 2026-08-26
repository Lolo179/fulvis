package com.fulvis.application.item.stock;

public record GetItemStockResult(String itemId, int stockTotal, int stockReserved, int stockAvailable) {

    public GetItemStockResult {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("itemId must not be blank");
        }
    }
}
