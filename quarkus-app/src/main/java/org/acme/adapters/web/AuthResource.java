package org.acme.adapters.web;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.acme.adapters.keycloak.KeycloakPasswordAuthenticator;
import org.acme.core.AuthSessionService;
import org.acme.core.UserService;
import org.acme.logging.StructuredLogContext;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Path("/api/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger LOG = Logger.getLogger(AuthResource.class);

    @Inject
    KeycloakPasswordAuthenticator authenticator;

    @Inject
    AuthSessionService authSessionService;

    @Inject
    UserService userService;

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        long startedAt = System.nanoTime();
        if (request == null || blank(request.username()) || blank(request.password())) {
            try (var ignored = StructuredLogContext.open(Map.of(
                    "event", "auth.login.failed",
                    "outcome", "validation_error",
                    "httpStatus", 400
            ))) {
                LOG.warn("auth.login.failed");
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Informe login ou e-mail e senha."))
                    .build();
        }

        try {
            var authenticated = authenticator.authenticate(request.username().trim(), request.password());
            var user = userService.findOrCreateByExternalId(
                    authenticated.subject(),
                    authenticated.username(),
                    authenticated.email()
            );

            var session = authSessionService.createSession(
                    new AuthSessionService.UserSession(authenticated.subject(), authenticated.username(), authenticated.email()),
                    authenticated.accessToken(),
                    authenticated.accessTokenExpiresAt(),
                    authenticated.refreshToken(),
                    authenticated.refreshTokenExpiresAt()
            );

                long durationMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
                try (var ignored = StructuredLogContext.open(Map.of(
                    "event", "auth.login.succeeded",
                    "outcome", "success",
                    "userId", authenticated.subject(),
                    "sessionId", session.id(),
                    "durationMs", durationMs,
                    "httpStatus", 200
                ))) {
                LOG.info("auth.login.succeeded");
                }

            return Response.ok(new UserDto(user))
                    .cookie(sessionCookie(session.id(), session.refreshTokenExpiresAt()))
                    .build();
        } catch (KeycloakPasswordAuthenticator.KeycloakAuthenticationException e) {
            if (e.failureType() == KeycloakPasswordAuthenticator.FailureType.INVALID_CREDENTIALS) {
                long durationMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
                try (var ignored = StructuredLogContext.open(Map.of(
                    "event", "auth.login.failed",
                    "outcome", "invalid_credentials",
                    "durationMs", durationMs,
                    "httpStatus", 401
                ))) {
                    LOG.warn("auth.login.failed");
                }
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("Login ou senha invalidos."))
                        .build();
            }

                long durationMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
                try (var ignored = StructuredLogContext.open(Map.of(
                    "event", "auth.login.failed",
                    "outcome", "auth_provider_unavailable",
                    "durationMs", durationMs,
                    "httpStatus", 503
                ))) {
                LOG.error("auth.login.failed", e);
                }

            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(new ErrorResponse("Servico de autenticacao temporariamente indisponivel."))
                    .build();
        }
    }

    @DELETE
    @Path("/session")
    public Response logout(@CookieParam(AuthSessionService.COOKIE_NAME) String sessionId) {
        authSessionService.logout(sessionId);
        try (var ignored = StructuredLogContext.open(Map.of(
                "event", "auth.logout.succeeded",
                "outcome", "success",
                "sessionId", sessionId == null ? "anonymous" : sessionId,
                "httpStatus", 204
        ))) {
            LOG.info("auth.logout.succeeded");
        }
        return Response.noContent()
                .cookie(expiredSessionCookie())
                .build();
    }

    private NewCookie sessionCookie(String sessionId, Instant expiresAt) {
        int maxAge = Math.max(0, (int) Duration.between(Instant.now(), expiresAt).getSeconds());
        return new NewCookie.Builder(AuthSessionService.COOKIE_NAME)
                .value(sessionId)
                .path("/")
                .httpOnly(true)
                .sameSite(NewCookie.SameSite.LAX)
                .maxAge(maxAge)
                .build();
    }

    private NewCookie expiredSessionCookie() {
        return new NewCookie.Builder(AuthSessionService.COOKIE_NAME)
                .value("")
                .path("/")
                .httpOnly(true)
                .sameSite(NewCookie.SameSite.LAX)
                .maxAge(0)
                .build();
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public record LoginRequest(String username, String password) {
    }

    public record ErrorResponse(String message) {
    }
}