package org.acme.adapters.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.acme.domain.PhoneNumber;

@Entity
@Table(name = "phone_numbers", indexes = {
        @jakarta.persistence.Index(name = "idx_phone_numbers_contact_status", columnList = "contact_id, status")
})
public class PhoneNumberEntity extends AgendaBaseEntity {

    @Column(name = "number", nullable = false)
    public String number;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contact_id", nullable = false)
    public ContactEntity contact;

    public PhoneNumber toDomain() {
        return new PhoneNumber(id, number, createdAt, updatedAt, status);
    }

    public static PhoneNumberEntity fromDomain(PhoneNumber phoneNumber, ContactEntity contact) {
        PhoneNumberEntity entity = new PhoneNumberEntity();
        entity.id = phoneNumber.id;
        entity.number = phoneNumber.number;
        entity.createdAt = phoneNumber.createdAt;
        entity.updatedAt = phoneNumber.updatedAt;
        entity.status = phoneNumber.status;
        entity.contact = contact;
        return entity;
    }
}

