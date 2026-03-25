package org.acme.adapters.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.acme.core.AuthSessionRepository;
import org.acme.core.AuthSessionService;

import java.time.Instant;
import java.util.Optional;

@ApplicationScoped
public class AuthSessionRepositoryImpl implements AuthSessionRepository {

    @Override
    public Optional<AuthSessionService.SessionData> findById(String sessionId) {
        AuthSessionEntity entity = AuthSessionEntity.findById(sessionId);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(entity.toDomain());
    }

    @Override
    @Transactional
    public Optional<AuthSessionService.SessionData> findByIdForUpdate(String sessionId) {
        AuthSessionEntity entity = AuthSessionEntity.findById(sessionId, LockModeType.PESSIMISTIC_WRITE);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(entity.toDomain());
    }

    @Override
    @Transactional
    public AuthSessionService.SessionData save(AuthSessionService.SessionData sessionData) {
        AuthSessionEntity entity = AuthSessionEntity.findById(sessionData.id());
        if (entity == null) {
            entity = AuthSessionEntity.fromDomain(sessionData);
            entity.persist();
            return entity.toDomain();
        }

        entity.subject = sessionData.user().subject();
        entity.username = sessionData.user().username();
        entity.email = sessionData.user().email();
        entity.accessToken = sessionData.accessToken();
        entity.accessTokenExpiresAt = sessionData.accessTokenExpiresAt();
        entity.refreshToken = sessionData.refreshToken();
        entity.refreshTokenExpiresAt = sessionData.refreshTokenExpiresAt();
        return entity.toDomain();
    }

    @Override
    @Transactional
    public void deleteById(String sessionId) {
        AuthSessionEntity.deleteById(sessionId);
    }

    @Override
    @Transactional
    public long deleteExpiredSessions() {
        return AuthSessionEntity.delete("refreshTokenExpiresAt < ?1", Instant.now());
    }
}