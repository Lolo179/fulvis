package com.fulvis.adapters.in.rest.error;

import com.fulvis.application.error.ConcurrencyConflictException;
import com.fulvis.application.order.ItemNotFoundException;
import com.fulvis.application.order.OrderNotFoundException;
import com.fulvis.domain.item.InsufficientStockException;
import com.fulvis.domain.order.InvalidOrderLinesException;
import com.fulvis.domain.order.InvalidOrderTransitionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

public class RestErrorMapper {

    public ResponseEntity<RestErrorResponse> toResponse(Throwable error, String traceId) {
        Objects.requireNonNull(error, "error must not be null");
        ErrorMapping mapping = map(error);
        String responseTraceId = mapping.errorCode() == ConceptualErrorCode.MissingTraceId
                ? "missing"
                : TraceIdValidator.requireTraceId(traceId);

        return ResponseEntity
                .status(mapping.status())
                .body(new RestErrorResponse(mapping.errorCode().name(), safeMessage(error, mapping), responseTraceId));
    }

    private static ErrorMapping map(Throwable error) {
        if (error instanceof OrderNotFoundException) {
            return new ErrorMapping(ConceptualErrorCode.OrderNotFound, HttpStatus.NOT_FOUND);
        }
        if (error instanceof ItemNotFoundException) {
            return new ErrorMapping(ConceptualErrorCode.ItemNotFound, HttpStatus.NOT_FOUND);
        }
        if (error instanceof InvalidOrderTransitionException) {
            return new ErrorMapping(ConceptualErrorCode.InvalidOrderTransition, HttpStatus.CONFLICT);
        }
        if (error instanceof InsufficientStockException) {
            return new ErrorMapping(ConceptualErrorCode.InsufficientStock, HttpStatus.CONFLICT);
        }
        if (error instanceof InvalidOrderLinesException || error instanceof IllegalArgumentException) {
            return new ErrorMapping(ConceptualErrorCode.InvalidOrderLines, HttpStatus.BAD_REQUEST);
        }
        if (error instanceof ConcurrencyConflictException) {
            return new ErrorMapping(ConceptualErrorCode.ConcurrencyConflict, HttpStatus.CONFLICT);
        }
        if (error instanceof MissingTraceIdException) {
            return new ErrorMapping(ConceptualErrorCode.MissingTraceId, HttpStatus.BAD_REQUEST);
        }
        return new ErrorMapping(ConceptualErrorCode.UnexpectedError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static String safeMessage(Throwable error, ErrorMapping mapping) {
        if (mapping.errorCode() == ConceptualErrorCode.UnexpectedError) {
            return "Unexpected error";
        }
        if (error.getMessage() == null || error.getMessage().isBlank()) {
            return mapping.errorCode().name();
        }
        return error.getMessage();
    }

    private record ErrorMapping(ConceptualErrorCode errorCode, HttpStatus status) {
    }
}
