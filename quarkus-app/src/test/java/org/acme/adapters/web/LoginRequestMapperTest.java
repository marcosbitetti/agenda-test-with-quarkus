package org.acme.adapters.web;

import org.acme.core.AuthLoginInput;
import org.acme.i18n.AgendaMessages;
import org.acme.i18n.MessageKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LoginRequestMapperTest {

    @Test
    public void mapLoginRequestToInput() {
        LoginRequest request = new LoginRequest(" joao ", "12345678");

        AuthLoginInput input = LoginRequestMapper.toInput(request);

        assertEquals("joao", input.username());
        assertEquals("12345678", input.password());
    }

    @Test
    public void rejectNullRequest() {
        NullPointerException error = assertThrows(NullPointerException.class,
                () -> LoginRequestMapper.toInput(null));

        assertEquals(AgendaMessages.get(MessageKey.AUTH_LOGIN_REQUIRED), error.getMessage());
    }

    @Test
    public void rejectBlankUsername() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> LoginRequestMapper.toInput(new LoginRequest("   ", "12345678")));

        assertEquals(AgendaMessages.get(MessageKey.AUTH_LOGIN_REQUIRED), error.getMessage());
    }

    @Test
    public void rejectBlankPassword() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> LoginRequestMapper.toInput(new LoginRequest("joao", "   ")));

        assertEquals(AgendaMessages.get(MessageKey.AUTH_LOGIN_REQUIRED), error.getMessage());
    }
}