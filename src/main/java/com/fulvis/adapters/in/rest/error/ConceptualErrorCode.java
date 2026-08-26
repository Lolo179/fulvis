package com.fulvis.adapters.in.rest.error;

public enum ConceptualErrorCode {
    OrderNotFound,
    ItemNotFound,
    InvalidOrderTransition,
    InsufficientStock,
    InvalidOrderLines,
    ConcurrencyConflict,
    MissingTraceId,
    UnexpectedError
}
