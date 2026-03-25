package org.acme.domain;

import org.acme.i18n.AgendaMessages;
import org.acme.i18n.MessageKey;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

public class Contact extends AgendaEntity {
    private Long ownerUserId;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private List<PhoneNumber> phoneNumbers;
    private String relationshipDegree;

    public Contact() {
    }

    public Contact(final Long id,
                   final Long ownerUserId,
                   final String firstName,
                   final String lastName,
                   final LocalDate birthDate,
                   final List<PhoneNumber> phoneNumbers,
                   final String relationshipDegree,
                   final OffsetDateTime createdAt,
                   final OffsetDateTime updatedAt,
                   final Status status) {
        super(id, createdAt, updatedAt, status);
        this.ownerUserId = Objects.requireNonNull(ownerUserId, AgendaMessages.get(MessageKey.OWNER_USER_REQUIRED));
        this.firstName = requireText(firstName, AgendaMessages.get(MessageKey.FIRST_NAME_REQUIRED));
        this.lastName = requireText(lastName, AgendaMessages.get(MessageKey.LAST_NAME_REQUIRED));
        this.birthDate = Objects.requireNonNull(birthDate, AgendaMessages.get(MessageKey.BIRTH_DATE_REQUIRED));
        this.phoneNumbers = requirePhoneNumbers(phoneNumbers);
        this.relationshipDegree = normalizeOptional(relationshipDegree);
    }

    public String fullName() {
        return firstName + " " + lastName;
    }

    private String requireText(String value, String message) {
        Objects.requireNonNull(value, message);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return trimmed;
    }
    private List<PhoneNumber> requirePhoneNumbers(final List<PhoneNumber> phoneNumbers) {
        Objects.requireNonNull(phoneNumbers, AgendaMessages.get(MessageKey.PHONE_REQUIRED));
        if (phoneNumbers.isEmpty()) {
            throw new IllegalArgumentException(AgendaMessages.get(MessageKey.PHONE_REQUIRED));
        }
        if (phoneNumbers.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(AgendaMessages.get(MessageKey.PHONE_INVALID));
        }
        return List.copyOf(phoneNumbers);
    }

    private String normalizeOptional(final String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public List<PhoneNumber> getPhoneNumbers() {
        return phoneNumbers;
    }

    public String getRelationshipDegree() {
        return relationshipDegree;
    }
}
