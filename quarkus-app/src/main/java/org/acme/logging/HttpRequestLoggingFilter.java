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

    public static final String REQUEST_ID_KEY = "requestId";
    public static final String HTTP_METHOD_KEY = "httpMethod";
    public static final String REQUEST_PATH_KEY = "requestPath";
    public static final String CLIENT_IP_KEY = "clientIp";
    public static final String USER_AGENT_KEY = "userAgent";
    public static final String REQUEST_STARTED_AT_KEY = "requestStartedAt";
    private static final String REQUEST_SCOPE_PROPERTY = "agenda.request.scope";
    private static final String REQUEST_STARTED_AT_PROPERTY = "agenda.request.startedAt";
    private static final String REQUEST_STARTED_NANOS_PROPERTY = "agenda.request.startedNanos";
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
        String userAgent = Optional.ofNullable(requestContext.getHeaderString("User-Agent")).orElse("unknown");

        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put(REQUEST_ID_KEY, requestId);
        fields.put(HTTP_METHOD_KEY, method);
        fields.put(REQUEST_PATH_KEY, requestPath);
        fields.put(CLIENT_IP_KEY, clientIp);
        fields.put(USER_AGENT_KEY, userAgent);
        fields.put(REQUEST_STARTED_AT_KEY, startedAt.toString());

        StructuredLogContext.Scope scope = StructuredLogContext.open(fields);
        requestContext.setProperty(REQUEST_SCOPE_PROPERTY, scope);
        requestContext.setProperty(REQUEST_STARTED_AT_PROPERTY, startedAt);
        requestContext.setProperty(REQUEST_STARTED_NANOS_PROPERTY, startedNanos);

        try (var ignored = StructuredLogContext.open(Map.of(
                "event", "http.request.started",
                "outcome", "in_progress"
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
                "event", "http.request.completed",
                "outcome", responseContext.getStatus() >= 400 ? "error" : "success",
                "httpStatus", responseContext.getStatus(),
                "requestFinishedAt", finishedAt.toString(),
                "durationMs", durationMs
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
        String forwardedFor = requestContext.getHeaderString("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        if (routingContext != null && routingContext.request() != null && routingContext.request().remoteAddress() != null) {
            return routingContext.request().remoteAddress().hostAddress();
        }

        return "unknown";
    }
}