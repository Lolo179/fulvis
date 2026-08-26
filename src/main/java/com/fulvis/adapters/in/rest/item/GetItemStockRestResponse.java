package com.fulvis.adapters.in.rest.item;

public record GetItemStockRestResponse(String itemId, int stockTotal, int stockReserved, int stockAvailable) {
}
