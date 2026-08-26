package com.fulvis.application.order;

public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(String itemId) {
        super("Item not found: " + itemId);
    }
}
