package org.acme.adapters.web;

import org.acme.core.ContactWriteInput;
import org.acme.i18n.AgendaMessages;
import org.acme.i18n.MessageKey;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ContactRequestMapperTest {

    @Test
    public void mapRequestToWriteInput() {
        CreateContactRequest request = new CreateContactRequest("Maria", "Silva", LocalDate.of(1992, 7, 10),
                List.of("11999990000", "1133334444"), "Prima");

        ContactWriteInput input = ContactRequestMapper.toWriteInput(request);

        assertEquals("Maria", input.firstName());
        assertEquals("Silva", input.lastName());
        assertEquals(LocalDate.of(1992, 7, 10), input.birthDate());
        assertEquals(List.of("11999990000", "1133334444"), input.phoneNumbers());
        assertEquals("Prima", input.relationshipDegree());
    }

    @Test
    public void copyPhoneNumberListDefensively() {
        List<String> phoneNumbers = new ArrayList<>(List.of("11999990000"));
        CreateContactRequest request = new CreateContactRequest("Maria", "Silva", LocalDate.of(1992, 7, 10),
                phoneNumbers, "Prima");

        ContactWriteInput input = ContactRequestMapper.toWriteInput(request);

        assertNotSame(phoneNumbers, input.phoneNumbers());
        assertEquals(List.of("11999990000"), input.phoneNumbers());
    }

    @Test
    public void rejectNullRequest() {
        NullPointerException error = assertThrows(NullPointerException.class,
                () -> ContactRequestMapper.toWriteInput(null));

        assertEquals(AgendaMessages.get(MessageKey.CONTACT_DATA_REQUIRED), error.getMessage());
    }
}