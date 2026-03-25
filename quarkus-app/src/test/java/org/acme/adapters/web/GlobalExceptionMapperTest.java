package org.acme.adapters.web;

import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.core.Response;
import org.acme.i18n.AgendaMessages;
import org.acme.i18n.MessageKey;
import org.acme.logging.StructuredLogContext;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GlobalExceptionMapperTest {

    @Test
    public void internalServerErrorProducesEnriched500Payload() {
        GlobalExceptionMapper mapper = new GlobalExceptionMapper();

        try (var ignored = StructuredLogContext.open(Map.of(
                "requestId", "req-500",
                "httpMethod", "POST",
                "requestPath", "/api/contacts"
        ))) {
            Response response = mapper.toResponse(new RuntimeException(AgendaMessages.get(MessageKey.UNEXPECTED_FAILURE)));

            assertEquals(500, response.getStatus());
            assertEquals("application/json", response.getMediaType().toString());

            Object entity = response.getEntity();
            assertTrue(entity instanceof ApiErrorResponse);
            ApiErrorResponse error = (ApiErrorResponse) entity;
            assertEquals(AgendaMessages.get(MessageKey.UNEXPECTED_FAILURE), error.message());
            assertEquals("req-500", error.requestId());
            assertEquals("POST", error.method());
            assertEquals("/api/contacts", error.path());
            assertEquals(500, error.status());
            assertNotNull(error.timestamp());
            assertTrue(!error.timestamp().isAfter(Instant.now()));
        }
    }

    @Test
    public void serviceUnavailableProducesEnriched503Payload() {
        GlobalExceptionMapper mapper = new GlobalExceptionMapper();

        try (var ignored = StructuredLogContext.open(Map.of(
                "requestId", "req-503",
                "httpMethod", "GET",
                "requestPath", "/api/users/me"
        ))) {
            Response response = mapper.toResponse(new ServiceUnavailableException(AgendaMessages.get(MessageKey.AUTH_SERVICE_UNAVAILABLE)));

            assertEquals(503, response.getStatus());
            assertEquals("application/json", response.getMediaType().toString());

            Object entity = response.getEntity();
            assertTrue(entity instanceof ApiErrorResponse);
            ApiErrorResponse error = (ApiErrorResponse) entity;
            assertEquals(AgendaMessages.get(MessageKey.AUTH_SERVICE_UNAVAILABLE), error.message());
            assertEquals("req-503", error.requestId());
            assertEquals("GET", error.method());
            assertEquals("/api/users/me", error.path());
            assertEquals(503, error.status());
            assertNotNull(error.timestamp());
            assertTrue(!error.timestamp().isAfter(Instant.now()));
        }
    }

    @Test
    public void serviceUnavailableIsNotTreatedAsExpectedClientError() {
        GlobalExceptionMapper mapper = new GlobalExceptionMapper();

        assertFalse(mapper.isExpectedClientError(Response.Status.SERVICE_UNAVAILABLE.getStatusCode()));
    }

    @Test
    public void internalServerErrorIsNotTreatedAsExpectedClientError() {
        GlobalExceptionMapper mapper = new GlobalExceptionMapper();

        assertFalse(mapper.isExpectedClientError(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
    }
}