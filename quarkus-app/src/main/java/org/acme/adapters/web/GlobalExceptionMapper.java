package org.acme.adapters.web;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.acme.i18n.AgendaMessages;
import org.acme.i18n.MessageKey;
import org.acme.logging.StructuredLogFields;
import org.acme.logging.StructuredLogContext;
import org.jboss.logging.Logger;

import java.util.Map;

@Provider
@ApplicationScoped
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Throwable throwable) {
        int status = resolveStatus(throwable);
        String message = resolveMessage(throwable, status);
        boolean expectedClientError = isExpectedClientError(status);

        try (var ignored = StructuredLogContext.open(Map.of(StructuredLogFields.EVENT, "http.request.failed",
                StructuredLogFields.OUTCOME, expectedClientError ? "client_error" : "exception",
                StructuredLogFields.HTTP_STATUS, status, StructuredLogFields.EXCEPTION_TYPE,
                throwable.getClass().getName(), StructuredLogFields.EXCEPTION_MESSAGE, throwable.getMessage()))) {
            if (expectedClientError) {
                LOG.warnf("http.request.failed status=%s type=%s message=%s", status,
                        throwable.getClass().getSimpleName(), message);
            } else {
                LOG.error("http.request.failed", throwable);
            }
        }

        return Response.status(status).type(MediaType.APPLICATION_JSON)
                .entity(ApiErrorResponse.current(message, status)).build();
    }

    private int resolveStatus(Throwable throwable) {
        if (throwable instanceof WebApplicationException webApplicationException) {
            return webApplicationException.getResponse().getStatus();
        }
        return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
    }

    private String resolveMessage(Throwable throwable, int status) {
        if (throwable instanceof NotAuthorizedException) {
            return AgendaMessages.get(MessageKey.AUTH_SESSION_INVALID_OR_EXPIRED);
        }
        if (throwable instanceof NotFoundException) {
            return fallback(throwable.getMessage(), AgendaMessages.get(MessageKey.RESOURCE_NOT_FOUND));
        }
        if (throwable instanceof NotAllowedException) {
            return fallback(throwable.getMessage(), AgendaMessages.get(MessageKey.METHOD_NOT_ALLOWED));
        }
        if (throwable instanceof WebApplicationException) {
            return fallback(normalizeMessage(throwable.getMessage()),
                    Response.Status.fromStatusCode(status).getReasonPhrase());
        }
        return fallback(normalizeMessage(throwable.getMessage()),
                AgendaMessages.get(MessageKey.REQUEST_INTERNAL_FAILURE));
    }

    private String fallback(String message, String fallback) {
        return message == null || message.isBlank() ? fallback : message;
    }

    private String normalizeMessage(String message) {
        if (message == null || message.isBlank() || message.startsWith("HTTP ")) {
            return null;
        }
        return message;
    }

    boolean isExpectedClientError(int status) {
        return status == Response.Status.BAD_REQUEST.getStatusCode()
                || status == Response.Status.UNAUTHORIZED.getStatusCode()
                || status == Response.Status.NOT_FOUND.getStatusCode()
                || status == Response.Status.METHOD_NOT_ALLOWED.getStatusCode();
    }
}
