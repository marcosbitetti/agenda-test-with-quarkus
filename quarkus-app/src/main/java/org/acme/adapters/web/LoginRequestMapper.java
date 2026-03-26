package org.acme.adapters.web;

import org.acme.core.AuthLoginInput;
import org.acme.i18n.AgendaMessages;
import org.acme.i18n.MessageKey;

import java.util.Objects;

public final class LoginRequestMapper {

    private LoginRequestMapper() {
    }

    public static AuthLoginInput toInput(final LoginRequest request) {
        LoginRequest source = Objects.requireNonNull(request,
                () -> AgendaMessages.get(MessageKey.AUTH_LOGIN_REQUIRED));
        String username = requireText(source.username());
        String password = requireText(source.password());
        return new AuthLoginInput(username.trim(), password);
    }

    private static String requireText(final String value) {
        Objects.requireNonNull(value, AgendaMessages.get(MessageKey.AUTH_LOGIN_REQUIRED));
        if (value.isBlank()) {
            throw new IllegalArgumentException(AgendaMessages.get(MessageKey.AUTH_LOGIN_REQUIRED));
        }
        return value;
    }
}