package com.fulvis.domain.order;

public class InvalidOrderTransitionException extends RuntimeException {

    public InvalidOrderTransitionException(String message) {
        super(message);
    }
}
