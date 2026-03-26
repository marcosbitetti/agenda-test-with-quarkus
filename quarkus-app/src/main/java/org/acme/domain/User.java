package org.acme.domain;

import java.time.OffsetDateTime;
import java.util.Objects;

public final class User {
    private final Long id;
    private final String externalId; // sub claim from OIDC
    private final String username;
    private final String email;
    private final OffsetDateTime createdAt;

    public User(final Long idParam, final String externalIdParam, final String usernameParam, final String emailParam,
            final OffsetDateTime createdAtParam) {
        this.id = idParam;
        this.externalId = externalIdParam;
        this.username = usernameParam;
        this.email = emailParam;
        this.createdAt = createdAtParam;
    }

    public boolean hasIdentity(final String usernameParam, final String emailParam) {
        return Objects.equals(username, usernameParam) && Objects.equals(email, emailParam);
    }

    public User withIdentity(final String usernameParam, final String emailParam) {
        return new User(id, externalId, usernameParam, emailParam, createdAt);
    }

    public static User newUser(final String externalIdParam, final String usernameParam, final String emailParam,
            final OffsetDateTime createdAtParam) {
        return new User(null, externalIdParam, usernameParam, emailParam, createdAtParam);
    }

    public Long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
