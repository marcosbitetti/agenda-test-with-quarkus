package org.acme.core;

import java.util.Optional;

public interface AuthSessionRepository {
    Optional<AuthSessionService.SessionData> findById(String sessionId);

    Optional<AuthSessionService.SessionData> findByIdForUpdate(String sessionId);

    AuthSessionService.SessionData save(AuthSessionService.SessionData sessionData);

    void deleteById(String sessionId);

    long deleteExpiredSessions();
}
