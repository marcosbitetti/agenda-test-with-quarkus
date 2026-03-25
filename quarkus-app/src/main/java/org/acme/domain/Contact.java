package org.acme.domain;

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
        this.ownerUserId = Objects.requireNonNull(ownerUserId, "Usuario dono obrigatorio.");
        this.firstName = requireText(firstName, "Nome obrigatorio.");
        this.lastName = requireText(lastName, "Sobrenome obrigatorio.");
        this.birthDate = Objects.requireNonNull(birthDate, "Data de nascimento obrigatoria.");
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
        Objects.requireNonNull(phoneNumbers, "Telefone obrigatorio.");
        if (phoneNumbers.isEmpty()) {
            throw new IllegalArgumentException("Telefone obrigatorio.");
        }
        if (phoneNumbers.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Telefone invalido.");
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