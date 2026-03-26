package org.acme.domain;

import org.acme.i18n.AgendaMessages;
import org.acme.i18n.MessageKey;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

public final class Contact extends AgendaEntity {
    private Long ownerUserId;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private List<PhoneNumber> phoneNumbers;
    private String relationshipDegree;

    public Contact() {
    }

    public Contact(final Long idParam, final Long ownerUserIdParam, final String firstNameParam,
            final String lastNameParam, final LocalDate birthDateParam, final List<PhoneNumber> phoneNumbersParam,
            final String relationshipDegreeParam, final OffsetDateTime createdAtParam,
            final OffsetDateTime updatedAtParam, final Status statusParam) {
        super(idParam, createdAtParam, updatedAtParam, statusParam);
        this.ownerUserId = Objects.requireNonNull(ownerUserIdParam, AgendaMessages.get(MessageKey.OWNER_USER_REQUIRED));
        this.firstName = requireText(firstNameParam, AgendaMessages.get(MessageKey.FIRST_NAME_REQUIRED));
        this.lastName = requireText(lastNameParam, AgendaMessages.get(MessageKey.LAST_NAME_REQUIRED));
        this.birthDate = Objects.requireNonNull(birthDateParam, AgendaMessages.get(MessageKey.BIRTH_DATE_REQUIRED));
        this.phoneNumbers = requirePhoneNumbers(phoneNumbersParam);
        this.relationshipDegree = normalizeOptional(relationshipDegreeParam);
    }

    public String fullName() {
        return firstName + " " + lastName;
    }

    public List<String> phoneNumberValues() {
        return phoneNumbers.stream().map(PhoneNumber::getNumber).toList();
    }

    public static List<PhoneNumber> createActivePhoneNumbers(final List<String> phoneNumbersParam,
            final OffsetDateTime timestamp) {
        Objects.requireNonNull(phoneNumbersParam, AgendaMessages.get(MessageKey.PHONE_REQUIRED));
        if (phoneNumbersParam.isEmpty()) {
            throw new IllegalArgumentException(AgendaMessages.get(MessageKey.PHONE_REQUIRED));
        }
        return phoneNumbersParam.stream()
                .map(number -> new PhoneNumber(null, number, timestamp, timestamp, IAgendaEntity.Status.ACTIVE))
                .toList();
    }

    private String requireText(final String value, final String message) {
        Objects.requireNonNull(value, message);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return trimmed;
    }

    private List<PhoneNumber> requirePhoneNumbers(final List<PhoneNumber> phoneNumbersParam) {
        Objects.requireNonNull(phoneNumbersParam, AgendaMessages.get(MessageKey.PHONE_REQUIRED));
        if (phoneNumbersParam.isEmpty()) {
            throw new IllegalArgumentException(AgendaMessages.get(MessageKey.PHONE_REQUIRED));
        }
        if (phoneNumbersParam.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(AgendaMessages.get(MessageKey.PHONE_INVALID));
        }
        return List.copyOf(phoneNumbersParam);
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

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long bId;
        private Long bOwnerUserId;
        private String bFirstName;
        private String bLastName;
        private LocalDate bBirthDate;
        private List<PhoneNumber> bPhoneNumbers;
        private String bRelationshipDegree;
        private OffsetDateTime bCreatedAt;
        private OffsetDateTime bUpdatedAt;
        private IAgendaEntity.Status bStatus;

        public Builder id(final Long idParam) {
            this.bId = idParam;
            return this;
        }

        public Builder ownerUserId(final Long ownerUserIdParam) {
            this.bOwnerUserId = ownerUserIdParam;
            return this;
        }

        public Builder firstName(final String firstNameParam) {
            this.bFirstName = firstNameParam;
            return this;
        }

        public Builder lastName(final String lastNameParam) {
            this.bLastName = lastNameParam;
            return this;
        }

        public Builder birthDate(final LocalDate birthDateParam) {
            this.bBirthDate = birthDateParam;
            return this;
        }

        public Builder phoneNumbers(final List<PhoneNumber> phoneNumbersParam) {
            this.bPhoneNumbers = phoneNumbersParam;
            return this;
        }

        public Builder relationshipDegree(final String relationshipDegreeParam) {
            this.bRelationshipDegree = relationshipDegreeParam;
            return this;
        }

        public Builder createdAt(final OffsetDateTime createdAtParam) {
            this.bCreatedAt = createdAtParam;
            return this;
        }

        public Builder updatedAt(final OffsetDateTime updatedAtParam) {
            this.bUpdatedAt = updatedAtParam;
            return this;
        }

        public Builder status(final IAgendaEntity.Status statusParam) {
            this.bStatus = statusParam;
            return this;
        }

        public Contact build() {
            return new Contact(bId, bOwnerUserId, bFirstName, bLastName, bBirthDate, bPhoneNumbers, bRelationshipDegree,
                    bCreatedAt, bUpdatedAt, bStatus);
        }
    }
}
