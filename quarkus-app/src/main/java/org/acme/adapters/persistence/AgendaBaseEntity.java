package org.acme.adapters.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.acme.domain.IAgendaEntity;

import java.time.OffsetDateTime;

@MappedSuperclass
public abstract class AgendaBaseEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "created_at", nullable = false)
    public OffsetDateTime createdAt;

    @Column(name = "updated_at")
    public OffsetDateTime updatedAt;

    @Convert(converter = AgendaStatusConverter.class)
    @Column(name = "status", nullable = false)
    public IAgendaEntity.Status status;
}
