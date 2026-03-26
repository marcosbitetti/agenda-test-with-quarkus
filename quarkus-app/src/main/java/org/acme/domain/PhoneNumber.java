package org.acme.domain;

import org.acme.i18n.AgendaMessages;
import org.acme.i18n.MessageKey;

import java.time.OffsetDateTime;
import java.util.Objects;

public final class PhoneNumber extends AgendaEntity {
    private String number;

    public PhoneNumber() {
    }

    public PhoneNumber(final Long idParam, final String numberParam, final OffsetDateTime createdAtParam,
            final OffsetDateTime updatedAtParam, final Status statusParam) {
        super(idParam, createdAtParam, updatedAtParam, statusParam);
        this.number = requireText(numberParam, AgendaMessages.get(MessageKey.PHONE_REQUIRED));
    }

    private String requireText(final String value, final String message) {
        Objects.requireNonNull(value, message);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return trimmed;
    }

    public String getNumber() {
        return number;
    }
}
