package org.acme.logging;

import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Provider
@ApplicationScoped
@Priority(Priorities.AUTHENTICATION)
public class HttpRequestLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String REQUEST_SCOPE_PROPERTY = "agenda.request.scope";
    private static final String REQUEST_STARTED_AT_PROPERTY = "agenda.request.startedAt";
    private static final String REQUEST_STARTED_NANOS_PROPERTY = "agenda.request.startedNanos";
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";
    private static final String UNKNOWN_VALUE = "unknown";
    private static final Logger LOG = Logger.getLogger(HttpRequestLoggingFilter.class);

    @Inject
    RoutingContext routingContext;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String requestId = UUID.randomUUID().toString();
        Instant startedAt = Instant.now();
        long startedNanos = System.nanoTime();
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();
        String query = requestContext.getUriInfo().getRequestUri().getQuery();
        String requestPath = query == null || query.isBlank() ? path : path + "?" + query;
        String clientIp = resolveClientIp(requestContext);
        String userAgent = Optional.ofNullable(requestContext.getHeaderString(HttpHeaders.USER_AGENT)).orElse(UNKNOWN_VALUE);

        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put(StructuredLogFields.REQUEST_ID, requestId);
        fields.put(StructuredLogFields.HTTP_METHOD, method);
        fields.put(StructuredLogFields.REQUEST_PATH, requestPath);
        fields.put(StructuredLogFields.CLIENT_IP, clientIp);
        fields.put(StructuredLogFields.USER_AGENT, userAgent);
        fields.put(StructuredLogFields.REQUEST_STARTED_AT, startedAt.toString());

        StructuredLogContext.Scope scope = StructuredLogContext.open(fields);
        requestContext.setProperty(REQUEST_SCOPE_PROPERTY, scope);
        requestContext.setProperty(REQUEST_STARTED_AT_PROPERTY, startedAt);
        requestContext.setProperty(REQUEST_STARTED_NANOS_PROPERTY, startedNanos);

        try (var ignored = StructuredLogContext.open(Map.of(
        StructuredLogFields.EVENT, "http.request.started",
        StructuredLogFields.OUTCOME, "in_progress"
        ))) {
            LOG.info("http.request.started");
            LOG.debugf("http.request.started method=%s path=%s ip=%s userAgent=%s", method, requestPath, clientIp, userAgent);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        Instant finishedAt = Instant.now();
        Instant startedAt = (Instant) requestContext.getProperty(REQUEST_STARTED_AT_PROPERTY);
        Long startedNanos = (Long) requestContext.getProperty(REQUEST_STARTED_NANOS_PROPERTY);
        long durationMs = startedNanos == null ? -1L : Duration.ofNanos(System.nanoTime() - startedNanos).toMillis();

        try (var ignored = StructuredLogContext.open(Map.of(
            StructuredLogFields.EVENT, "http.request.completed",
            StructuredLogFields.OUTCOME, responseContext.getStatus() >= 400 ? "error" : "success",
            StructuredLogFields.HTTP_STATUS, responseContext.getStatus(),
            StructuredLogFields.REQUEST_FINISHED_AT, finishedAt.toString(),
            StructuredLogFields.DURATION_MS, durationMs
        ))) {
            LOG.info("http.request.completed");
            LOG.debugf("http.request.completed status=%d durationMs=%d startedAt=%s finishedAt=%s",
                    responseContext.getStatus(),
                    durationMs,
                    startedAt,
                    finishedAt);
        } finally {
            Object scope = requestContext.getProperty(REQUEST_SCOPE_PROPERTY);
            if (scope instanceof StructuredLogContext.Scope structuredScope) {
                structuredScope.close();
            }
        }
    }

    private String resolveClientIp(ContainerRequestContext requestContext) {
        String forwardedFor = requestContext.getHeaderString(FORWARDED_FOR_HEADER);
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        if (routingContext != null && routingContext.request() != null && routingContext.request().remoteAddress() != null) {
            return routingContext.request().remoteAddress().hostAddress();
        }

        return UNKNOWN_VALUE;
    }
}