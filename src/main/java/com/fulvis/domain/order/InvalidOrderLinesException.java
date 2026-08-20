package com.fulvis.domain.order;

public class InvalidOrderLinesException extends RuntimeException {

    public InvalidOrderLinesException(String message) {
        super(message);
    }
}
