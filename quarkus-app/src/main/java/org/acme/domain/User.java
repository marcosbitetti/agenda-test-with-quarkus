package org.acme.domain;

import java.time.OffsetDateTime;

public final class User {
    private Long id;
    private String externalId; // sub claim from OIDC
    private String username;
    private String email;
    private OffsetDateTime createdAt;

    public User() {
    }

    public User(final Long idParam, final String externalIdParam, final String usernameParam, final String emailParam,
            final OffsetDateTime createdAtParam) {
        this.id = idParam;
        this.externalId = externalIdParam;
        this.username = usernameParam;
        this.email = emailParam;
        this.createdAt = createdAtParam;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
