package org.acme.domain;

import org.acme.i18n.AgendaMessages;
import org.acme.i18n.MessageKey;

import java.time.OffsetDateTime;
import java.util.Objects;

public abstract class AgendaEntity implements IAgendaEntity {

    public Long id;
    public OffsetDateTime createdAt;
    public OffsetDateTime updatedAt;
    public Status status;

    protected AgendaEntity() {
    }

    protected AgendaEntity(Long id, OffsetDateTime createdAt, OffsetDateTime updatedAt, Status status) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = requireStatus(status);
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
    public void softDelete(OffsetDateTime updatedAt) {
        OffsetDateTime timestamp = Objects.requireNonNull(updatedAt, AgendaMessages.get(MessageKey.UPDATED_AT_REQUIRED));
        this.status = Status.DELETED;
        this.updatedAt = timestamp;
    }

    protected Status requireStatus(Status status) {
        if (status == null) {
            throw new IllegalArgumentException(AgendaMessages.get(MessageKey.STATUS_INVALID));
        }
        return status;
    }
}