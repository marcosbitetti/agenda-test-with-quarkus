package org.acme.adapters.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.acme.domain.Contact;
import org.acme.domain.IAgendaEntity;
import org.acme.domain.PhoneNumber;

import java.util.List;

@Entity
@Table(name = "contacts", indexes = {
        @jakarta.persistence.Index(name = "idx_contacts_owner_status", columnList = "owner_user_id, status") })
/**
 * JPA entity mapping for contacts.
 * <p>
 * This class provides conversion helpers between persistence and domain models.
 */
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
        final List<PhoneNumber> phones;
        if (phoneNumbers == null) {
            phones = List.of();
        } else {
            phones = phoneNumbers.stream().filter(phoneNumber -> phoneNumber.status == IAgendaEntity.Status.ACTIVE)
                    .map(PhoneNumberEntity::toDomain).toList();
        }

        return new Contact(super.getId(), ownerUserId, firstName, lastName, birthDate, phones, relationshipDegree, super.getCreatedAt(),
                super.getUpdatedAt(), super.getStatus());
    }

    public static ContactEntity fromDomain(final Contact contact) {
        ContactEntity entity = new ContactEntity();
        entity.id = contact.getId();
        entity.ownerUserId = contact.getOwnerUserId();
        entity.firstName = contact.getFirstName();
        entity.lastName = contact.getLastName();
        entity.birthDate = contact.getBirthDate();
        entity.relationshipDegree = contact.getRelationshipDegree();
        entity.createdAt = contact.getCreatedAt();
        entity.updatedAt = contact.getUpdatedAt();
        entity.status = contact.getStatus();

        if (contact.getPhoneNumbers() == null) {
            entity.phoneNumbers = List.of();
        } else {
            entity.phoneNumbers = contact.getPhoneNumbers().stream()
                    .map(phoneNumber -> PhoneNumberEntity.fromDomain(phoneNumber, entity)).toList();
        }

        return entity;
    }
}
