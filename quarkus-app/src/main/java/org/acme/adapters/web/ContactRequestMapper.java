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
        List<String> phoneNumbers = source.phoneNumbers() == null ? null : List.copyOf(source.phoneNumbers());
        return new ContactWriteInput(source.firstName(), source.lastName(), source.birthDate(), phoneNumbers,
                source.relationshipDegree());
    }
}