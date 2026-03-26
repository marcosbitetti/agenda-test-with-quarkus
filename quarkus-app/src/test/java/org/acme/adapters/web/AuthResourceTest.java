package org.acme.adapters.web;

import org.acme.adapters.keycloak.KeycloakPasswordAuthenticator;
import org.acme.core.AuthSessionCreateInput;
import org.acme.core.AuthSessionService;
import org.acme.core.UserService;
import org.acme.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthResourceTest {

    @Mock
    KeycloakPasswordAuthenticator authenticator;

    @Mock
    AuthSessionService authSessionService;

    @Mock
    UserService userService;

    @InjectMocks
    AuthResource authResource;

    @Test
    public void rejectNullLoginRequestAsBadRequest() {
        var response = authResource.login(null);

        assertEquals(400, response.getStatus());
    }

    @Test
    public void doNotMaskInternalFailureAsBadRequest() {
        var authenticated = new KeycloakPasswordAuthenticator.AuthenticatedUser("sub-123", "joao",
                "joao@example.com", "access-token", Instant.parse("2026-03-26T11:40:00Z"), "refresh-token",
                Instant.parse("2026-03-26T12:40:00Z"));
        var user = new User(1L, "sub-123", "joao", "joao@example.com",
                OffsetDateTime.parse("2026-03-26T10:15:30Z"));

        when(authenticator.authenticate("joao", "12345678")).thenReturn(authenticated);
        when(userService.findOrCreateByExternalId("sub-123", "joao", "joao@example.com")).thenReturn(user);
        when(authSessionService.createSession(any(AuthSessionCreateInput.class)))
                .thenThrow(new NullPointerException("boom"));

        assertThrows(NullPointerException.class,
                () -> authResource.login(new LoginRequest("joao", "12345678")));
    }
}