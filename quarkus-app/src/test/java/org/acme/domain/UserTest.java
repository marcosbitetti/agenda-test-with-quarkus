package org.acme.domain;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserTest {

    @Test
    public void detectSameIdentityData() {
        User user = new User(1L, "ext-123", "joao", "joao@example.com", OffsetDateTime.parse("2026-03-25T10:15:30Z"));

        assertTrue(user.hasIdentity("joao", "joao@example.com"));
        assertFalse(user.hasIdentity("maria", "joao@example.com"));
    }

    @Test
    public void rebuildWithUpdatedIdentity() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-03-25T10:15:30Z");
        User user = new User(1L, "ext-123", "joao", "joao@old.example.com", createdAt);

        User updated = user.withIdentity("joao", "joao@example.com");

        assertEquals(1L, updated.getId());
        assertEquals("ext-123", updated.getExternalId());
        assertEquals("joao", updated.getUsername());
        assertEquals("joao@example.com", updated.getEmail());
        assertEquals(createdAt, updated.getCreatedAt());
    }
}