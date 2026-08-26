package com.fulvis.adapters.in.rest.error;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TraceIdValidatorTest {

    @Test
    void givenTraceIdWhenRequiredThenItIsReturned() {
        assertThat(TraceIdValidator.requireTraceId("trace-123")).isEqualTo("trace-123");
    }

    @Test
    void givenMissingTraceIdWhenRequiredThenItFails() {
        assertThatThrownBy(() -> TraceIdValidator.requireTraceId(null))
                .isInstanceOf(MissingTraceIdException.class);
        assertThatThrownBy(() -> TraceIdValidator.requireTraceId(" "))
                .isInstanceOf(MissingTraceIdException.class);
    }
}
