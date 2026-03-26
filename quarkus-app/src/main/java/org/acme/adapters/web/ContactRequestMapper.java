package org.acme.adapters.web;

import org.acme.core.ContactWriteInput;
import org.acme.i18n.AgendaMessages;
import org.acme.i18n.MessageKey;

import java.util.List;
import java.util.Objects;

public final class ContactRequestMapper {

    private ContactRequestMapper() {
    }

    public static ContactWriteInput toWriteInput(final CreateContactRequest request) {
        CreateContactRequest source = Objects.requireNonNull(request,
                () -> AgendaMessages.get(MessageKey.CONTACT_DATA_REQUIRED));
        List<String> phoneNumbers = source.phoneNumbers() == null ? null
                : source.phoneNumbers().stream().map(ContactRequestMapper::sanitizePhoneNumber).toList();
        return new ContactWriteInput(source.firstName(), source.lastName(), source.birthDate(), phoneNumbers,
                source.relationshipDegree());
    }

    private static String sanitizePhoneNumber(final String phoneNumber) {
        Objects.requireNonNull(phoneNumber, AgendaMessages.get(MessageKey.PHONE_REQUIRED));

        StringBuilder digits = new StringBuilder(phoneNumber.length());
        for (int index = 0; index < phoneNumber.length(); index++) {
            char current = phoneNumber.charAt(index);
            if (Character.isDigit(current)) {
                digits.append(current);
            }
        }
        if (digits.isEmpty()) {
            throw new IllegalArgumentException(AgendaMessages.get(MessageKey.PHONE_REQUIRED));
        }
        return digits.toString();
    }
}