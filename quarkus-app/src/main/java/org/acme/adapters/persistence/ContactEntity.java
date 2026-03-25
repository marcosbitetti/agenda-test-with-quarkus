package org.acme.adapters.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.acme.domain.Contact;
import org.acme.domain.IAgendaEntity;

import java.util.List;

@Entity
@Table(name = "contacts", indexes = {
        @jakarta.persistence.Index(name = "idx_contacts_owner_status", columnList = "owner_user_id, status")
})
public class ContactEntity extends AgendaBaseEntity {

    @Column(name = "owner_user_id", nullable = false)
    public Long ownerUserId;

    @Column(name = "first_name", nullable = false)
    public String firstName;

    @Column(name = "last_name", nullable = false)
    public String lastName;

    @Column(name = "birth_date", nullable = false)
    public java.time.LocalDate birthDate;

    @Column(name = "relationship_degree")
    public String relationshipDegree;

    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<PhoneNumberEntity> phoneNumbers;

    public Contact toDomain() {
        return new Contact(
                id,
                ownerUserId,
                firstName,
                lastName,
                birthDate,
            phoneNumbers == null ? List.of() : phoneNumbers.stream()
                .filter(phoneNumber -> phoneNumber.status == IAgendaEntity.Status.ACTIVE)
                .map(PhoneNumberEntity::toDomain)
                .toList(),
                relationshipDegree,
                createdAt,
                updatedAt,
                status
        );
    }

    public static ContactEntity fromDomain(Contact contact) {
        ContactEntity entity = new ContactEntity();
        entity.id = contact.id;
        entity.ownerUserId = contact.ownerUserId;
        entity.firstName = contact.firstName;
        entity.lastName = contact.lastName;
        entity.birthDate = contact.birthDate;
        entity.relationshipDegree = contact.relationshipDegree;
        entity.createdAt = contact.createdAt;
        entity.updatedAt = contact.updatedAt;
        entity.status = contact.status;
        entity.phoneNumbers = contact.phoneNumbers == null ? List.of() : contact.phoneNumbers.stream()
                .map(phoneNumber -> PhoneNumberEntity.fromDomain(phoneNumber, entity))
                .toList();
        return entity;
    }
}
