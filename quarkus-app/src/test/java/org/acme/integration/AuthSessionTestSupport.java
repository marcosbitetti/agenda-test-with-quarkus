package org.acme.integration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.adapters.persistence.AuthSessionEntity;
import org.acme.core.AuthSessionService;

import java.time.Instant;

@ApplicationScoped
public class AuthSessionTestSupport {

    @Inject
    AuthSessionService authSessionService;

    @Transactional
    public void expireAccessToken(String sessionId, Instant expiresAt) {
        AuthSessionEntity entity = AuthSessionEntity.findById(sessionId);
        if (entity != null) {
            entity.accessTokenExpiresAt = expiresAt;
        }
    }

    @Transactional
    public void expireRefreshToken(String sessionId, Instant expiresAt) {
        AuthSessionEntity entity = AuthSessionEntity.findById(sessionId);
        if (entity != null) {
            entity.refreshTokenExpiresAt = expiresAt;
        }
    }

    public long cleanupExpiredSessions() {
        return authSessionService.cleanupExpiredSessions();
    }
}