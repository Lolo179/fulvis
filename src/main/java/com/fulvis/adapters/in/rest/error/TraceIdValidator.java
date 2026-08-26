package com.fulvis.adapters.in.rest.error;

public final class TraceIdValidator {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    private TraceIdValidator() {
    }

    public static String requireTraceId(String traceId) {
        if (traceId == null || traceId.isBlank()) {
            throw new MissingTraceIdException();
        }
        return traceId;
    }
}
