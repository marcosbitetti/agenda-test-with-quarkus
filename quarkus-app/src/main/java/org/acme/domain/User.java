package org.acme.domain;

import java.time.OffsetDateTime;

public class User {
    public Long id;
    public String externalId; // sub claim from OIDC
    public String username;
    public String email;
    public OffsetDateTime createdAt;

    public User() {}

    public User(Long id, String externalId, String username, String email, OffsetDateTime createdAt) {
        this.id = id;
        this.externalId = externalId;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
    }
}
