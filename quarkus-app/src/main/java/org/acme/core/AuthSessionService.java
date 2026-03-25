package org.acme.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.adapters.keycloak.KeycloakPasswordAuthenticator;
import org.acme.i18n.AgendaMessages;
import org.acme.i18n.MessageKey;
import org.acme.logging.StructuredLogContext;
import org.jboss.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AuthSessionService {

    public static final String COOKIE_NAME = "AGENDA_SESSION";
    private static final Logger LOG = Logger.getLogger(AuthSessionService.class);

    @Inject
    AuthSessionRepository authSessionRepository;

    @Inject
    KeycloakPasswordAuthenticator authenticator;

    @ConfigProperty(name = "agenda.auth.refresh-skew-seconds", defaultValue = "15")
    long refreshSkewSeconds;

    public SessionData createSession(UserSession user,
                                     String accessToken,
                                     Instant accessTokenExpiresAt,
                                     String refreshToken,
                                     Instant refreshTokenExpiresAt) {
        String sessionId = UUID.randomUUID().toString();
        SessionData session = new SessionData(
                sessionId,
                user,
                accessToken,
                accessTokenExpiresAt,
                refreshToken,
                refreshTokenExpiresAt,
                Instant.now()
        );
        return authSessionRepository.save(session);
    }

    @Transactional
    public Optional<SessionData> findActiveSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            LOG.debug("auth.session.lookup.missing_cookie");
            return Optional.empty();
        }

        Optional<SessionData> storedSession = authSessionRepository.findByIdForUpdate(sessionId);
        if (storedSession.isEmpty()) {
            LOG.debugf("auth.session.lookup.not_found sessionId=%s", sessionId);
            return Optional.empty();
        }

        SessionData session = storedSession.get();
        Instant now = Instant.now();
        if (session.refreshTokenExpiresAt().isBefore(now)) {
            authSessionRepository.deleteById(sessionId);
            try (var ignored = StructuredLogContext.open(Map.of(
                    "event", "auth.session.expired",
                    "outcome", "refresh_token_expired",
                    "sessionId", sessionId,
                    "userId", session.user().subject()
            ))) {
                LOG.info("auth.session.expired");
            }
            return Optional.empty();
        }

        if (session.accessTokenExpiresAt().minusSeconds(refreshSkewSeconds).isAfter(now)) {
            LOG.debugf("auth.session.lookup.hit sessionId=%s userId=%s", session.id(), session.user().subject());
            return Optional.of(session);
        }

        try {
            LOG.debugf("auth.session.refresh.required sessionId=%s userId=%s", session.id(), session.user().subject());
            long startedAt = System.nanoTime();
            var refreshed = authenticator.refresh(session.refreshToken());
            SessionData refreshedSession = new SessionData(
                    session.id(),
                    new UserSession(refreshed.subject(), refreshed.username(), refreshed.email()),
                    refreshed.accessToken(),
                    refreshed.accessTokenExpiresAt(),
                    refreshed.refreshToken(),
                    refreshed.refreshTokenExpiresAt(),
                    session.createdAt()
            );
            SessionData savedSession = authSessionRepository.save(refreshedSession);
            try (var ignored = StructuredLogContext.open(Map.of(
                    "event", "auth.session.refreshed",
                    "outcome", "success",
                    "sessionId", session.id(),
                    "userId", refreshed.subject(),
                    "durationMs", Duration.ofNanos(System.nanoTime() - startedAt).toMillis()
            ))) {
                LOG.info("auth.session.refreshed");
            }
            return Optional.of(savedSession);
        } catch (KeycloakPasswordAuthenticator.KeycloakAuthenticationException e) {
            if (e.failureType() == KeycloakPasswordAuthenticator.FailureType.REFRESH_REJECTED) {
                authSessionRepository.deleteById(sessionId);
                try (var ignored = StructuredLogContext.open(Map.of(
                        "event", "auth.session.refresh_failed",
                        "outcome", "refresh_rejected",
                        "sessionId", session.id(),
                        "userId", session.user().subject()
                ))) {
                    LOG.warn("auth.session.refresh_failed");
                }
                return Optional.empty();
            }
            try (var ignored = StructuredLogContext.open(Map.of(
                    "event", "auth.session.refresh_failed",
                    "outcome", "auth_provider_unavailable",
                    "sessionId", session.id(),
                    "userId", session.user().subject()
            ))) {
                LOG.error("auth.session.refresh_failed", e);
            }
            throw new SessionUnavailableException(AgendaMessages.get(MessageKey.AUTH_SESSION_REFRESH_FAILED), e);
        }
    }

    public void invalidate(String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            LOG.debugf("auth.session.invalidate sessionId=%s", sessionId);
            authSessionRepository.deleteById(sessionId);
        }
    }

    public long cleanupExpiredSessions() {
        long deleted = authSessionRepository.deleteExpiredSessions();
        if (deleted > 0) {
            try (var ignored = StructuredLogContext.open(Map.of(
                    "event", "auth.session.cleanup.completed",
                    "outcome", "success",
                    "deletedSessions", deleted
            ))) {
                LOG.info("auth.session.cleanup.completed");
            }
        }
        return deleted;
    }

    public void logout(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            LOG.debug("auth.logout.skipped_missing_session");
            return;
        }

        LOG.debugf("auth.logout.started sessionId=%s", sessionId);
        Optional<SessionData> session = authSessionRepository.findById(sessionId);
        try {
            session.ifPresent(current -> authenticator.logout(current.refreshToken()));
        } catch (KeycloakPasswordAuthenticator.KeycloakAuthenticationException ignored) {
            session.ifPresent(current -> {
                try (var context = StructuredLogContext.open(Map.of(
                        "event", "auth.logout.remote_failed",
                        "outcome", "auth_provider_unavailable",
                        "sessionId", current.id(),
                        "userId", current.user().subject()
                ))) {
                    LOG.warn("auth.logout.remote_failed");
                }
            });
        } finally {
            authSessionRepository.deleteById(sessionId);
        }
    }

    public record SessionData(String id,
                              UserSession user,
                              String accessToken,
                              Instant accessTokenExpiresAt,
                              String refreshToken,
                              Instant refreshTokenExpiresAt,
                              Instant createdAt) {
    }

    public record UserSession(String subject, String username, String email) {
    }

    public static class SessionUnavailableException extends RuntimeException {
        public SessionUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}