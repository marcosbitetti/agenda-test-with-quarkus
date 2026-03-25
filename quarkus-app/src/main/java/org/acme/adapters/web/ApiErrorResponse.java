package org.acme.adapters.web;

import org.acme.logging.StructuredLogContext;

import java.time.Instant;

public record ApiErrorResponse(String message,
                               String requestId,
                               String method,
                               String path,
                               int status,
                               Instant timestamp) {

    public static ApiErrorResponse current(String message, int status) {
        return new ApiErrorResponse(
                message,
                StructuredLogContext.getString("requestId"),
                StructuredLogContext.getString("httpMethod"),
                StructuredLogContext.getString("requestPath"),
                status,
                Instant.now()
        );
    }
}