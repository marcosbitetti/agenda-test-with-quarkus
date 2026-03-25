package org.acme.domain;

import java.time.OffsetDateTime;
import java.util.Objects;

public class PhoneNumber extends AgendaEntity {
    public String number;

    public PhoneNumber() {
    }

    public PhoneNumber(Long id,
                       String number,
                       OffsetDateTime createdAt,
                       OffsetDateTime updatedAt,
                       Status status) {
        super(id, createdAt, updatedAt, status);
        this.number = requireText(number, "Telefone obrigatorio.");
    }

    private String requireText(String value, String message) {
        Objects.requireNonNull(value, message);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return trimmed;
    }
}