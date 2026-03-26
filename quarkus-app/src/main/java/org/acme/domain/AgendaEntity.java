package org.acme.domain;

import org.acme.i18n.AgendaMessages;
import org.acme.i18n.MessageKey;

import java.time.OffsetDateTime;
import java.util.Objects;

public abstract class AgendaEntity implements IAgendaEntity {

    private Long id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Status status;

    protected AgendaEntity() {
    }

    protected AgendaEntity(final Long idParam, final OffsetDateTime createdAtParam, final OffsetDateTime updatedAtParam,
            final Status statusParam) {
        this.id = idParam;
        this.createdAt = createdAtParam;
        this.updatedAt = updatedAtParam;
        this.status = requireStatus(statusParam);
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public boolean isDeleted() {
        return status == Status.DELETED;
    }

    @Override
    public void softDelete(final OffsetDateTime updatedAtParam) {
        OffsetDateTime timestamp = Objects.requireNonNull(updatedAtParam,
                AgendaMessages.get(MessageKey.UPDATED_AT_REQUIRED));
        this.status = Status.DELETED;
        this.updatedAt = timestamp;
    }

    protected Status requireStatus(final Status statusParam) {
        if (statusParam == null) {
            throw new IllegalArgumentException(AgendaMessages.get(MessageKey.STATUS_INVALID));
        }
        return statusParam;
    }
}
