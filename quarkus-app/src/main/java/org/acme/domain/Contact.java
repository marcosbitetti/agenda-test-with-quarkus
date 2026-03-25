package org.acme.domain;

import org.acme.i18n.AgendaMessages;
import org.acme.i18n.MessageKey;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

public class Contact extends AgendaEntity {
    public Long ownerUserId;
    public String firstName;
    public String lastName;
    public LocalDate birthDate;
    public List<PhoneNumber> phoneNumbers;
    public String relationshipDegree;

    public Contact() {
    }

    public Contact(Long id,
                   Long ownerUserId,
                   String firstName,
                   String lastName,
                   LocalDate birthDate,
                   List<PhoneNumber> phoneNumbers,
                   String relationshipDegree,
                   OffsetDateTime createdAt,
                   OffsetDateTime updatedAt,
                   Status status) {
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

    private List<PhoneNumber> requirePhoneNumbers(List<PhoneNumber> phoneNumbers) {
        Objects.requireNonNull(phoneNumbers, AgendaMessages.get(MessageKey.PHONE_REQUIRED));
        if (phoneNumbers.isEmpty()) {
            throw new IllegalArgumentException(AgendaMessages.get(MessageKey.PHONE_REQUIRED));
        }
        if (phoneNumbers.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(AgendaMessages.get(MessageKey.PHONE_INVALID));
        }
        return List.copyOf(phoneNumbers);
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
