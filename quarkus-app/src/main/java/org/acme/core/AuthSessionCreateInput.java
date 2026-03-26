package org.acme.core;

import java.time.Instant;

public record AuthSessionCreateInput(AuthSessionService.UserSession user, String accessToken,
        Instant accessTokenExpiresAt, String refreshToken, Instant refreshTokenExpiresAt) {
}