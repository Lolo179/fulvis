package com.fulvis.adapters.in.rest.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    private final RestErrorMapper errorMapper = new RestErrorMapper();

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestErrorResponse> handle(
            Exception error,
            HttpServletRequest request
    ) {
        return errorMapper.toResponse(error, request.getHeader(TraceIdValidator.TRACE_ID_HEADER));
    }
}
