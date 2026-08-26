package com.fulvis.adapters.in.rest.error;

public class MissingTraceIdException extends RuntimeException {

    public MissingTraceIdException() {
        super("X-Trace-Id header is required");
    }
}
