package org.acme.domain;

import java.time.OffsetDateTime;

public class User {
    private Long id;
    private String externalId; // sub claim from OIDC
    private String username;
    private String email;
    private OffsetDateTime createdAt;

    public User() {}

    public User(final Long id, final String externalId, final String username, final String email, final OffsetDateTime createdAt) {
        this.id = id;
        this.externalId = externalId;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
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
