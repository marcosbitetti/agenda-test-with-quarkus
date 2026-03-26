package org.acme.adapters.web;

import org.acme.adapters.keycloak.KeycloakPasswordAuthenticator;
import org.acme.core.AuthSessionCreateInput;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AuthSessionCreateInputMapperTest {

    @Test
    public void mapAuthenticatedUserToSessionCreateInput() {
        Instant accessExpiresAt = Instant.parse("2026-03-26T11:40:00Z");
        Instant refreshExpiresAt = Instant.parse("2026-03-26T12:40:00Z");
        KeycloakPasswordAuthenticator.AuthenticatedUser authenticatedUser = new KeycloakPasswordAuthenticator.AuthenticatedUser(
                "sub-123", "joao", "joao@example.com", "access-token", accessExpiresAt, "refresh-token",
                refreshExpiresAt);

        AuthSessionCreateInput input = AuthSessionCreateInputMapper.toInput(authenticatedUser);

        assertEquals("sub-123", input.user().subject());
        assertEquals("joao", input.user().username());
        assertEquals("joao@example.com", input.user().email());
        assertEquals("access-token", input.accessToken());
        assertEquals(accessExpiresAt, input.accessTokenExpiresAt());
        assertEquals("refresh-token", input.refreshToken());
        assertEquals(refreshExpiresAt, input.refreshTokenExpiresAt());
    }

    @Test
    public void rejectNullAuthenticatedUser() {
        assertThrows(NullPointerException.class, () -> AuthSessionCreateInputMapper.toInput(null));
    }
}