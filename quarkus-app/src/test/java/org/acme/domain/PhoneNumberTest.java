package org.acme.domain;

import org.acme.domain.IAgendaEntity.Status;
import org.acme.i18n.AgendaMessages;
import org.acme.i18n.MessageKey;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PhoneNumberTest {

    @Test
    public void createPhoneNumberWithTrimmedValue() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-03-25T10:15:30Z");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-03-25T12:30:00Z");
        PhoneNumber result = new PhoneNumber(1L, " 11999999999 ", createdAt, updatedAt, Status.ACTIVE);

        assertEquals(1L, result.getId());
        assertEquals("11999999999", result.getNumber());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(updatedAt, result.getUpdatedAt());
        assertEquals(Status.ACTIVE, result.getStatus());
    }

    @Test
    public void phoneNumberInheritsAgendaEntityContract() {
        PhoneNumber result = new PhoneNumber(1L, "11999999999", null, null, Status.ACTIVE);

        assertEquals(Status.ACTIVE, result.getStatus());
    }

    @Test
    public void rejectBlankPhoneNumber() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new PhoneNumber(null, "   ", null, null, Status.ACTIVE));

        assertEquals(AgendaMessages.get(MessageKey.PHONE_REQUIRED), error.getMessage());
    }

    @Test
    public void softDeletePhoneNumber() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-03-25T10:15:30Z");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-03-25T12:30:00Z");
        OffsetDateTime deletedAt = OffsetDateTime.parse("2026-03-26T08:00:00Z");
        PhoneNumber result = new PhoneNumber(1L, "11999999999", createdAt, updatedAt, Status.ACTIVE);

        result.softDelete(deletedAt);

        assertEquals(deletedAt, result.getUpdatedAt());
        assertEquals(Status.DELETED, result.getStatus());
        assertEquals(true, result.isDeleted());
    }

    @Test
    public void rejectInvalidStatus() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new PhoneNumber(1L, "11999999999", null, null, null));

        assertEquals(AgendaMessages.get(MessageKey.STATUS_INVALID), error.getMessage());
    }
}
