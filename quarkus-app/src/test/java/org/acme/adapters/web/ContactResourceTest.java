package org.acme.adapters.web;

import org.acme.core.AuthSessionService;
import org.acme.core.ContactService;
import org.acme.core.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class ContactResourceTest {

    @Mock
    AuthSessionService authSessionService;

    @Mock
    UserService userService;

    @Mock
    ContactService contactService;

    @InjectMocks
    ContactResource contactResource;

    @Test
    public void rejectNullCreateRequestBeforeAuthentication() {
        var response = contactResource.create("invalid-session", null);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(authSessionService, userService, contactService);
    }

    @Test
    public void rejectNullUpdateRequestBeforeAuthentication() {
        var response = contactResource.update("invalid-session", 10L, null);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(authSessionService, userService, contactService);
    }
}