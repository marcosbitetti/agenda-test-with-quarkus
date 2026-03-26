package org.acme.adapters.web;

import org.acme.domain.Contact;

import java.util.List;
import java.util.Objects;

public final class ContactMapper {

    private ContactMapper() {
    }

    public static ContactDto toDto(final Contact contact) {
        Contact source = Objects.requireNonNull(contact, "contact is required");
        return new ContactDto(source.getId(), source.getFirstName(), source.getLastName(), source.fullName(),
                source.getBirthDate(), source.phoneNumberValues(), source.getRelationshipDegree());
    }

    public static List<ContactDto> toDtos(final List<Contact> contacts) {
        return Objects.requireNonNull(contacts, "contacts are required").stream().map(ContactMapper::toDto).toList();
    }
}