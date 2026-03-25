package org.acme.domain;

import java.time.OffsetDateTime;

public interface IAgendaEntity {
    enum Status {
        ACTIVE,
        DELETED
    }

    Long getId();

    OffsetDateTime getCreatedAt();

    OffsetDateTime getUpdatedAt();

    Status getStatus();

    boolean isDeleted();

    void softDelete(OffsetDateTime updatedAt);
}
