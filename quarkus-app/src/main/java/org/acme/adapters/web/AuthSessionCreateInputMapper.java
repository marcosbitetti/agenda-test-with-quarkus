package org.acme.adapters.web;

import org.acme.adapters.keycloak.KeycloakPasswordAuthenticator;
import org.acme.core.AuthSessionCreateInput;
import org.acme.core.AuthSessionService;

import java.util.Objects;

public final class AuthSessionCreateInputMapper {

    private AuthSessionCreateInputMapper() {
    }

    public static AuthSessionCreateInput toInput(final KeycloakPasswordAuthenticator.AuthenticatedUser authenticatedUser) {
        KeycloakPasswordAuthenticator.AuthenticatedUser source = Objects.requireNonNull(authenticatedUser,
                "authenticatedUser is required");
        return new AuthSessionCreateInput(
                new AuthSessionService.UserSession(source.subject(), source.username(), source.email()),
                source.accessToken(), source.accessTokenExpiresAt(), source.refreshToken(),
                source.refreshTokenExpiresAt());
    }
}