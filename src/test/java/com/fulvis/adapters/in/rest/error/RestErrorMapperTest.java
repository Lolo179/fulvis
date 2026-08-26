package com.fulvis.adapters.in.rest.error;

import com.fulvis.application.error.ConcurrencyConflictException;
import com.fulvis.application.order.ItemNotFoundException;
import com.fulvis.application.order.OrderNotFoundException;
import com.fulvis.domain.item.InsufficientStockException;
import com.fulvis.domain.order.InvalidOrderLinesException;
import com.fulvis.domain.order.InvalidOrderTransitionException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestErrorMapperTest {

    private static final String TRACE_ID = "trace-123";
    private final RestErrorMapper mapper = new RestErrorMapper();

    @Test
    void givenOrderNotFoundWhenMappingThenItReturnsNotFound() {
        ResponseEntity<RestErrorResponse> response = mapper.toResponse(new OrderNotFoundException("order-123"), TRACE_ID);

        assertError(response, HttpStatus.NOT_FOUND, "OrderNotFound", TRACE_ID);
    }

    @Test
    void givenItemNotFoundWhenMappingThenItReturnsNotFound() {
        ResponseEntity<RestErrorResponse> response = mapper.toResponse(new ItemNotFoundException("item-123"), TRACE_ID);

        assertError(response, HttpStatus.NOT_FOUND, "ItemNotFound", TRACE_ID);
    }

    @Test
    void givenInvalidOrderTransitionWhenMappingThenItReturnsConflict() {
        ResponseEntity<RestErrorResponse> response = mapper.toResponse(new InvalidOrderTransitionException("invalid transition"), TRACE_ID);

        assertError(response, HttpStatus.CONFLICT, "InvalidOrderTransition", TRACE_ID);
    }

    @Test
    void givenInsufficientStockWhenMappingThenItReturnsConflict() {
        ResponseEntity<RestErrorResponse> response = mapper.toResponse(new InsufficientStockException("insufficient stock"), TRACE_ID);

        assertError(response, HttpStatus.CONFLICT, "InsufficientStock", TRACE_ID);
    }

    @Test
    void givenInvalidOrderLinesWhenMappingThenItReturnsBadRequest() {
        ResponseEntity<RestErrorResponse> response = mapper.toResponse(new InvalidOrderLinesException("invalid lines"), TRACE_ID);

        assertError(response, HttpStatus.BAD_REQUEST, "InvalidOrderLines", TRACE_ID);
    }

    @Test
    void givenIllegalArgumentWhenMappingThenItReturnsBadRequestAsInvalidOrderLines() {
        ResponseEntity<RestErrorResponse> response = mapper.toResponse(new IllegalArgumentException("invalid request"), TRACE_ID);

        assertError(response, HttpStatus.BAD_REQUEST, "InvalidOrderLines", TRACE_ID);
    }

    @Test
    void givenConcurrencyConflictWhenMappingThenItReturnsConflict() {
        ResponseEntity<RestErrorResponse> response = mapper.toResponse(new ConcurrencyConflictException("concurrency conflict"), TRACE_ID);

        assertError(response, HttpStatus.CONFLICT, "ConcurrencyConflict", TRACE_ID);
    }

    @Test
    void givenMissingTraceIdWhenMappingThenItReturnsBadRequestWithoutInternalDetails() {
        ResponseEntity<RestErrorResponse> response = mapper.toResponse(new MissingTraceIdException(), null);

        assertError(response, HttpStatus.BAD_REQUEST, "MissingTraceId", "missing");
        assertThat(response.getBody().message()).doesNotContain("Exception");
    }

    @Test
    void givenUnexpectedErrorWhenMappingThenItReturnsInternalServerErrorWithoutInternalDetails() {
        ResponseEntity<RestErrorResponse> response = mapper.toResponse(new RuntimeException("database stack trace"), TRACE_ID);

        assertError(response, HttpStatus.INTERNAL_SERVER_ERROR, "UnexpectedError", TRACE_ID);
        assertThat(response.getBody().message()).isEqualTo("Unexpected error");
    }

    @Test
    void givenBusinessErrorWithoutTraceIdWhenMappingThenItFailsBeforeCreatingResponse() {
        assertThatThrownBy(() -> mapper.toResponse(new OrderNotFoundException("order-123"), " "))
                .isInstanceOf(MissingTraceIdException.class);
    }

    private static void assertError(ResponseEntity<RestErrorResponse> response, HttpStatus status, String error, String traceId) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo(error);
        assertThat(response.getBody().message()).isNotBlank();
        assertThat(response.getBody().traceId()).isEqualTo(traceId);
    }
}
