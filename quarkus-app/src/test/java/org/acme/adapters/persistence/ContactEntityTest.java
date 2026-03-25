package org.acme.adapters.persistence;

import org.acme.domain.Contact;
import org.acme.domain.IAgendaEntity;
import org.acme.domain.PhoneNumber;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ContactEntityTest {

    @Test
    public void fromDomainMapsContactAndPhones() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-03-25T10:15:30Z");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-03-25T12:30:00Z");
        Contact contact = new Contact(
                10L,
                1L,
                "Maria",
                "Silva",
                LocalDate.of(1990, 5, 20),
                List.of(new PhoneNumber(20L, "11999999999", createdAt, updatedAt, IAgendaEntity.Status.ACTIVE)),
                "Irma",
                createdAt,
                updatedAt,
                IAgendaEntity.Status.ACTIVE
        );

        ContactEntity entity = ContactEntity.fromDomain(contact);

        assertEquals(10L, entity.id);
        assertEquals(1L, entity.ownerUserId);
        assertEquals("Maria", entity.firstName);
        assertEquals(1, entity.phoneNumbers.size());
        assertNotNull(entity.phoneNumbers.get(0).contact);
        assertEquals(entity, entity.phoneNumbers.get(0).contact);
    }

    @Test
    public void toDomainMapsContactAndPhones() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-03-25T10:15:30Z");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-03-25T12:30:00Z");
        ContactEntity entity = new ContactEntity();
        entity.id = 10L;
        entity.ownerUserId = 1L;
        entity.firstName = "Maria";
        entity.lastName = "Silva";
        entity.birthDate = LocalDate.of(1990, 5, 20);
        entity.relationshipDegree = "Irma";
        entity.createdAt = createdAt;
        entity.updatedAt = updatedAt;
        entity.status = IAgendaEntity.Status.ACTIVE;

        PhoneNumberEntity phoneNumberEntity = new PhoneNumberEntity();
        phoneNumberEntity.id = 20L;
        phoneNumberEntity.number = "11999999999";
        phoneNumberEntity.createdAt = createdAt;
        phoneNumberEntity.updatedAt = updatedAt;
        phoneNumberEntity.status = IAgendaEntity.Status.ACTIVE;
        phoneNumberEntity.contact = entity;
        entity.phoneNumbers = List.of(phoneNumberEntity);

        Contact contact = entity.toDomain();

        assertEquals(10L, contact.id);
        assertEquals(1L, contact.ownerUserId);
        assertEquals("Maria", contact.firstName);
        assertEquals(1, contact.phoneNumbers.size());
        assertEquals("11999999999", contact.phoneNumbers.get(0).number);
        assertEquals(IAgendaEntity.Status.ACTIVE, contact.status);
    }

    @Test
    public void toDomainIgnoresDeletedPhones() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-03-25T10:15:30Z");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-03-25T12:30:00Z");
        ContactEntity entity = new ContactEntity();
        entity.id = 10L;
        entity.ownerUserId = 1L;
        entity.firstName = "Maria";
        entity.lastName = "Silva";
        entity.birthDate = LocalDate.of(1990, 5, 20);
        entity.relationshipDegree = "Irma";
        entity.createdAt = createdAt;
        entity.updatedAt = updatedAt;
        entity.status = IAgendaEntity.Status.ACTIVE;

        PhoneNumberEntity activePhone = new PhoneNumberEntity();
        activePhone.id = 20L;
        activePhone.number = "11999999999";
        activePhone.createdAt = createdAt;
        activePhone.updatedAt = updatedAt;
        activePhone.status = IAgendaEntity.Status.ACTIVE;
        activePhone.contact = entity;

        PhoneNumberEntity deletedPhone = new PhoneNumberEntity();
        deletedPhone.id = 21L;
        deletedPhone.number = "11888887777";
        deletedPhone.createdAt = createdAt;
        deletedPhone.updatedAt = updatedAt;
        deletedPhone.status = IAgendaEntity.Status.DELETED;
        deletedPhone.contact = entity;

        entity.phoneNumbers = List.of(activePhone, deletedPhone);

        Contact contact = entity.toDomain();

        assertEquals(1, contact.phoneNumbers.size());
        assertEquals("11999999999", contact.phoneNumbers.get(0).number);
    }
}