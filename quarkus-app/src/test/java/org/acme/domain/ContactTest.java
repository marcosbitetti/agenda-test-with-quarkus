package org.acme.domain;

import org.acme.domain.IAgendaEntity.Status;
import org.acme.i18n.AgendaMessages;
import org.acme.i18n.MessageKey;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ContactTest {

    @Test
    public void createContactWithRequiredData() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-03-25T10:15:30Z");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-03-25T12:30:00Z");
        Contact result = new Contact(10L, 1L, " Maria ", " Silva ", LocalDate.of(1990, 5, 20),
                List.of(new PhoneNumber(null, "11999999999", createdAt, updatedAt, Status.ACTIVE),
                        new PhoneNumber(null, "1133334444", createdAt, updatedAt, Status.ACTIVE)),
                "  Irma ", createdAt, updatedAt, Status.ACTIVE);

        assertEquals(10L, result.getId());
        assertEquals(1L, result.getOwnerUserId());
        assertEquals("Maria", result.getFirstName());
        assertEquals("Silva", result.getLastName());
        assertEquals(LocalDate.of(1990, 5, 20), result.getBirthDate());
        assertEquals(2, result.getPhoneNumbers().size());
        assertEquals("Irma", result.getRelationshipDegree());
        assertEquals("Maria Silva", result.fullName());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(updatedAt, result.getUpdatedAt());
        assertEquals(Status.ACTIVE, result.getStatus());
    }

    @Test
    public void contactInheritsAgendaEntityContract() {
        Contact result = new Contact(null, 1L, "Joao", "Souza", LocalDate.of(1988, 1, 10),
                List.of(new PhoneNumber(null, "11999999999", null, null, Status.ACTIVE)), null, null, null,
                Status.ACTIVE);

        assertEquals(Status.ACTIVE, result.getStatus());
    }

    @Test
    public void normalizeOptionalRelationshipDegree() {
        Contact result = new Contact(null, 1L, "Joao", "Souza", LocalDate.of(1988, 1, 10),
                List.of(new PhoneNumber(null, "11999999999", null, null, Status.ACTIVE)), "   ", null, null,
                Status.ACTIVE);

        assertNull(result.getRelationshipDegree());
    }

    @Test
    public void projectPhoneNumberValues() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-03-25T10:15:30Z");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-03-25T12:30:00Z");
        Contact result = new Contact(null, 1L, "Joao", "Souza", LocalDate.of(1988, 1, 10),
                List.of(new PhoneNumber(null, "11999999999", createdAt, updatedAt, Status.ACTIVE),
                        new PhoneNumber(null, "1133334444", createdAt, updatedAt, Status.ACTIVE)),
                null, createdAt, updatedAt, Status.ACTIVE);

        assertEquals(List.of("11999999999", "1133334444"), result.phoneNumberValues());
    }

    @Test
    public void createActivePhoneNumbersFromValues() {
        OffsetDateTime now = OffsetDateTime.parse("2026-03-25T12:30:00Z");

        List<PhoneNumber> result = Contact.createActivePhoneNumbers(List.of("11999999999", "1133334444"), now);

        assertEquals(2, result.size());
        assertEquals("11999999999", result.get(0).getNumber());
        assertEquals(now, result.get(0).getCreatedAt());
        assertEquals(now, result.get(0).getUpdatedAt());
        assertEquals(Status.ACTIVE, result.get(0).getStatus());
    }

    @Test
    public void rejectEmptyActivePhoneNumbersSource() {
        OffsetDateTime now = OffsetDateTime.parse("2026-03-25T12:30:00Z");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> Contact.createActivePhoneNumbers(List.of(), now));

        assertEquals(AgendaMessages.get(MessageKey.PHONE_REQUIRED), error.getMessage());
    }

    @Test
    public void rejectMissingFirstName() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new Contact(null, 1L, " ", "Souza", LocalDate.now(),
                        List.of(new PhoneNumber(null, "11999999999", null, null, Status.ACTIVE)), null, null, null,
                        Status.ACTIVE));

        assertEquals(AgendaMessages.get(MessageKey.FIRST_NAME_REQUIRED), error.getMessage());
    }

    @Test
    public void rejectMissingLastName() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new Contact(null, 1L, "Joao", " ", LocalDate.now(),
                        List.of(new PhoneNumber(null, "11999999999", null, null, Status.ACTIVE)), null, null, null,
                        Status.ACTIVE));

        assertEquals(AgendaMessages.get(MessageKey.LAST_NAME_REQUIRED), error.getMessage());
    }

    @Test
    public void rejectMissingBirthDate() {
        NullPointerException error = assertThrows(NullPointerException.class,
                () -> new Contact(null, 1L, "Joao", "Souza", null,
                        List.of(new PhoneNumber(null, "11999999999", null, null, Status.ACTIVE)), null, null, null,
                        Status.ACTIVE));

        assertEquals(AgendaMessages.get(MessageKey.BIRTH_DATE_REQUIRED), error.getMessage());
    }

    @Test
    public void rejectEmptyPhoneNumbers() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> new Contact(null, 1L,
                "Joao", "Souza", LocalDate.now(), List.of(), null, null, null, Status.ACTIVE));

        assertEquals(AgendaMessages.get(MessageKey.PHONE_REQUIRED), error.getMessage());
    }

    @Test
    public void rejectNullPhoneNumberItem() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new Contact(null, 1L, "Joao", "Souza", LocalDate.now(),
                        Arrays.asList(new PhoneNumber(null, "11999999999", null, null, Status.ACTIVE), null), null,
                        null, null, Status.ACTIVE));

        assertEquals(AgendaMessages.get(MessageKey.PHONE_INVALID), error.getMessage());
    }

    @Test
    public void softDeleteContact() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-03-25T10:15:30Z");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-03-25T12:30:00Z");
        OffsetDateTime deletedAt = OffsetDateTime.parse("2026-03-26T08:00:00Z");
        Contact result = new Contact(10L, 1L, "Maria", "Silva", LocalDate.of(1990, 5, 20),
                List.of(new PhoneNumber(null, "11999999999", createdAt, updatedAt, Status.ACTIVE)), "Irma", createdAt,
                updatedAt, Status.ACTIVE);

        result.softDelete(deletedAt);

        assertEquals(deletedAt, result.getUpdatedAt());
        assertEquals(Status.DELETED, result.getStatus());
        assertEquals(true, result.isDeleted());
    }

    @Test
    public void rejectInvalidStatus() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new Contact(10L, 1L, "Maria", "Silva", LocalDate.of(1990, 5, 20),
                        List.of(new PhoneNumber(null, "11999999999", null, null, Status.ACTIVE)), null, null, null,
                        null));

        assertEquals(AgendaMessages.get(MessageKey.STATUS_INVALID), error.getMessage());
    }
}
