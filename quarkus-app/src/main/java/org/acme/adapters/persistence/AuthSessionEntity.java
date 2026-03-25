package org.acme.adapters.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.acme.core.AuthSessionService;

import java.time.Instant;

@Entity
@Table(name = "auth_sessions", indexes = {
    @jakarta.persistence.Index(name = "idx_auth_sessions_refresh_expires_at", columnList = "refresh_token_expires_at")
})
public class AuthSessionEntity extends PanacheEntityBase {

    @Id
    @Column(name = "session_id", nullable = false, updatable = false)
    public String sessionId;

    @Column(name = "subject", nullable = false)
    public String subject;

    @Column(name = "username", nullable = false)
    public String username;

    @Column(name = "email")
    public String email;

    @Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
    public String accessToken;

    @Column(name = "access_token_expires_at", nullable = false)
    public Instant accessTokenExpiresAt;

    @Column(name = "refresh_token", nullable = false, columnDefinition = "TEXT")
    public String refreshToken;

    @Column(name = "refresh_token_expires_at", nullable = false)
    public Instant refreshTokenExpiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Version
    @Column(name = "version", nullable = false)
    public long version;

    public AuthSessionService.SessionData toDomain() {
        return new AuthSessionService.SessionData(
                sessionId,
                new AuthSessionService.UserSession(subject, username, email),
                accessToken,
                accessTokenExpiresAt,
                refreshToken,
                refreshTokenExpiresAt,
                createdAt
        );
    }

    public static AuthSessionEntity fromDomain(AuthSessionService.SessionData sessionData) {
        AuthSessionEntity entity = new AuthSessionEntity();
        entity.sessionId = sessionData.id();
        entity.subject = sessionData.user().subject();
        entity.username = sessionData.user().username();
        entity.email = sessionData.user().email();
        entity.accessToken = sessionData.accessToken();
        entity.accessTokenExpiresAt = sessionData.accessTokenExpiresAt();
        entity.refreshToken = sessionData.refreshToken();
        entity.refreshTokenExpiresAt = sessionData.refreshTokenExpiresAt();
        entity.createdAt = sessionData.createdAt();
        return entity;
    }
}
