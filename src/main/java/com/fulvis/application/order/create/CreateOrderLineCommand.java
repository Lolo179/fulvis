package com.fulvis.application.order.create;

public record CreateOrderLineCommand(String itemId, int quantity) {
}
