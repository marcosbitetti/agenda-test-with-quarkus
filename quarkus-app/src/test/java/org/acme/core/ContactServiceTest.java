package org.acme.core;

import org.acme.domain.Contact;
import org.acme.domain.IAgendaEntity;
import org.acme.i18n.AgendaMessages;
import org.acme.i18n.MessageKey;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ContactServiceTest {

    @Mock
    ContactRepository contactRepository;

    @InjectMocks
    ContactService contactService;

    @Test
    public void createContactBuildsActivePhonesAndPersists() {
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Contact result = contactService.create(10L, "Maria", "Silva", LocalDate.of(1990, 5, 20),
                List.of("11999999999", "1133334444"), "Irma");

        assertNotNull(result);
        assertEquals(10L, result.ownerUserId);
        assertEquals("Maria", result.firstName);
        assertEquals(2, result.phoneNumbers.size());
        assertEquals(IAgendaEntity.Status.ACTIVE, result.status);
        assertEquals(IAgendaEntity.Status.ACTIVE, result.phoneNumbers.get(0).status);
        verify(contactRepository).save(any(Contact.class));
    }

    @Test
    public void createContactRejectsNullOwner() {
        NullPointerException error = assertThrows(NullPointerException.class,
                () -> contactService.create(null, "Maria", "Silva", LocalDate.now(), List.of("11999999999"), null));

        assertEquals(AgendaMessages.get(MessageKey.OWNER_USER_REQUIRED), error.getMessage());
    }

    @Test
    public void listActiveByOwnerDelegatesToRepository() {
        when(contactRepository.listActiveByOwnerUserId(10L)).thenReturn(List.of());

        contactService.listActiveByOwnerUserId(10L);

        verify(contactRepository).listActiveByOwnerUserId(10L);
    }

    @Test
    public void findActiveByIdAndOwnerDelegatesToRepository() {
        when(contactRepository.findActiveByIdAndOwnerUserId(5L, 10L)).thenReturn(Optional.empty());

        contactService.findActiveByIdAndOwnerUserId(5L, 10L);

        verify(contactRepository).findActiveByIdAndOwnerUserId(5L, 10L);
    }

    @Test
    public void softDeleteDelegatesWithTimestamp() {
        contactService.softDelete(5L, 10L);

        ArgumentCaptor<OffsetDateTime> captor = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(contactRepository).softDelete(org.mockito.ArgumentMatchers.eq(5L), org.mockito.ArgumentMatchers.eq(10L),
                captor.capture());
        assertNotNull(captor.getValue());
    }

    @Test
    public void updateExistingContactRebuildsPhonesAndPreservesIdentity() {
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);
        Contact existing = new Contact(
                5L, 10L, "Maria", "Silva", LocalDate.of(1990, 5, 20), List.of(new org.acme.domain.PhoneNumber(1L,
                        "11999990000", createdAt, createdAt, IAgendaEntity.Status.ACTIVE)),
                "Irma", createdAt, createdAt, IAgendaEntity.Status.ACTIVE);

        when(contactRepository.findActiveByIdAndOwnerUserId(5L, 10L)).thenReturn(Optional.of(existing));
        when(contactRepository.update(any(Contact.class)))
                .thenAnswer(invocation -> Optional.of(invocation.getArgument(0)));

        Optional<Contact> updated = contactService.update(10L, 5L, "Maria Clara", "Oliveira", LocalDate.of(1993, 8, 11),
                List.of("11911112222", "1133334444"), "Prima");

        assertTrue(updated.isPresent());
        assertEquals(5L, updated.orElseThrow().id);
        assertEquals(10L, updated.orElseThrow().ownerUserId);
        assertEquals("Maria Clara", updated.orElseThrow().firstName);
        assertEquals("Oliveira", updated.orElseThrow().lastName);
        assertEquals(2, updated.orElseThrow().phoneNumbers.size());
        verify(contactRepository).update(any(Contact.class));
    }

    @Test
    public void updateMissingContactReturnsEmpty() {
        when(contactRepository.findActiveByIdAndOwnerUserId(5L, 10L)).thenReturn(Optional.empty());

        Optional<Contact> updated = contactService.update(10L, 5L, "Maria", "Silva", LocalDate.of(1990, 5, 20),
                List.of("11999990000"), null);

        assertEquals(Optional.empty(), updated);
    }

    @Test
    public void updateRejectsNullOwner() {
        NullPointerException error = assertThrows(NullPointerException.class,
                () -> contactService.update(null, 5L, "Maria", "Silva", LocalDate.now(), List.of("11999990000"), null));

        assertEquals(AgendaMessages.get(MessageKey.OWNER_USER_REQUIRED), error.getMessage());
    }
}
