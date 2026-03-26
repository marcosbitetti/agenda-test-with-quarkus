package org.acme.adapters.web;

import org.acme.domain.Contact;
import org.acme.domain.IAgendaEntity;
import org.acme.domain.PhoneNumber;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ContactMapperTest {

    @Test
    public void mapContactToDto() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-03-25T10:15:30Z");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-03-25T12:30:00Z");
        Contact contact = new Contact(10L, 1L, "Maria", "Silva", LocalDate.of(1990, 5, 20),
                List.of(new PhoneNumber(20L, "11999999999", createdAt, updatedAt, IAgendaEntity.Status.ACTIVE)), "Prima",
                createdAt, updatedAt, IAgendaEntity.Status.ACTIVE);

        ContactDto dto = ContactMapper.toDto(contact);

        assertEquals(10L, dto.id());
        assertEquals("Maria", dto.firstName());
        assertEquals("Maria Silva", dto.fullName());
        assertEquals(List.of("11999999999"), dto.phoneNumbers());
        assertEquals("Prima", dto.relationshipDegree());
    }

    @Test
    public void mapContactListToDtos() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-03-25T10:15:30Z");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-03-25T12:30:00Z");
        Contact first = new Contact(10L, 1L, "Maria", "Silva", LocalDate.of(1990, 5, 20),
                List.of(new PhoneNumber(20L, "11999999999", createdAt, updatedAt, IAgendaEntity.Status.ACTIVE)), "Prima",
                createdAt, updatedAt, IAgendaEntity.Status.ACTIVE);
        Contact second = new Contact(11L, 1L, "Joao", "Souza", LocalDate.of(1988, 1, 10),
                List.of(new PhoneNumber(21L, "1133334444", createdAt, updatedAt, IAgendaEntity.Status.ACTIVE)), null,
                createdAt, updatedAt, IAgendaEntity.Status.ACTIVE);

        List<ContactDto> dtos = ContactMapper.toDtos(List.of(first, second));

        assertEquals(2, dtos.size());
        assertEquals(10L, dtos.get(0).id());
        assertEquals(11L, dtos.get(1).id());
    }

    @Test
    public void rejectNullContactListSource() {
        assertThrows(NullPointerException.class, () -> ContactMapper.toDtos(null));
    }
}