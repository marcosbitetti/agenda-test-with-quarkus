package org.acme.adapters.web;

import org.acme.core.AuthSessionService;
import org.acme.core.ContactWriteInput;
import org.acme.core.ContactService;
import org.acme.core.UserService;
import org.acme.domain.Contact;
import org.acme.domain.IAgendaEntity;
import org.acme.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

    @Test
    public void sanitizePhoneNumbersBeforeCallingService() {
        OffsetDateTime now = OffsetDateTime.parse("2026-03-26T12:30:00Z");
        AuthSessionService.SessionData session = new AuthSessionService.SessionData("session-id",
                new AuthSessionService.UserSession("sub-123", "joao", "joao@example.com"), "access-token",
                now.toInstant().plusSeconds(300), "refresh-token", now.toInstant().plusSeconds(600), now.toInstant());
        User user = new User(10L, "sub-123", "joao", "joao@example.com", now);
        Contact created = Contact.builder().id(20L).ownerUserId(10L).firstName("Maria").lastName("Silva")
                .birthDate(LocalDate.of(1992, 7, 10))
                .phoneNumbers(Contact.createActivePhoneNumbers(List.of("12988598514"), now))
                .relationshipDegree("Prima").createdAt(now).updatedAt(now).status(IAgendaEntity.Status.ACTIVE).build();

        when(authSessionService.findActiveSession("valid-session")).thenReturn(Optional.of(session));
        when(userService.findOrCreateByExternalId("sub-123", "joao", "joao@example.com")).thenReturn(user);
        when(contactService.create(eq(10L), any(ContactWriteInput.class))).thenReturn(created);

        var response = contactResource.create("valid-session", new CreateContactRequest("Maria", "Silva",
                LocalDate.of(1992, 7, 10), List.of("(12) 98859-8514"), "Prima"));

        ArgumentCaptor<ContactWriteInput> captor = ArgumentCaptor.forClass(ContactWriteInput.class);
        verify(contactService).create(eq(10L), captor.capture());
        assertEquals(201, response.getStatus());
        assertEquals(List.of("12988598514"), captor.getValue().phoneNumbers());
    }

    @Test
    public void sanitizePhoneNumbersOnUpdateBeforeCallingService() {
        OffsetDateTime now = OffsetDateTime.parse("2026-03-26T12:30:00Z");
        AuthSessionService.SessionData session = new AuthSessionService.SessionData("session-id",
                new AuthSessionService.UserSession("sub-123", "joao", "joao@example.com"), "access-token",
                now.toInstant().plusSeconds(300), "refresh-token", now.toInstant().plusSeconds(600), now.toInstant());
        User user = new User(10L, "sub-123", "joao", "joao@example.com", now);
        Contact updated = Contact.builder().id(20L).ownerUserId(10L).firstName("Maria").lastName("Silva")
                .birthDate(LocalDate.of(1992, 7, 10))
                .phoneNumbers(Contact.createActivePhoneNumbers(List.of("12988598514"), now))
                .relationshipDegree("Prima").createdAt(now).updatedAt(now).status(IAgendaEntity.Status.ACTIVE).build();

        when(authSessionService.findActiveSession("valid-session")).thenReturn(Optional.of(session));
        when(userService.findOrCreateByExternalId("sub-123", "joao", "joao@example.com")).thenReturn(user);
        when(contactService.update(eq(10L), eq(20L), any(ContactWriteInput.class))).thenReturn(Optional.of(updated));

        var response = contactResource.update("valid-session", 20L, new CreateContactRequest("Maria", "Silva",
                LocalDate.of(1992, 7, 10), List.of("(12) 98859-8514"), "Prima"));

        ArgumentCaptor<ContactWriteInput> captor = ArgumentCaptor.forClass(ContactWriteInput.class);
        verify(contactService).update(eq(10L), eq(20L), captor.capture());
        assertEquals(200, response.getStatus());
        assertEquals(List.of("12988598514"), captor.getValue().phoneNumbers());
    }
}