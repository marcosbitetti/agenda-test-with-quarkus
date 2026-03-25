package org.acme.adapters.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.core.ContactRepository;
import org.acme.domain.Contact;
import org.acme.domain.IAgendaEntity;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ContactRepositoryImpl implements ContactRepository {

    @Override
    public List<Contact> listActiveByOwnerUserId(Long ownerUserId) {
        return ContactEntity.find("ownerUserId = ?1 and status = ?2", ownerUserId, IAgendaEntity.Status.ACTIVE)
                .list()
                .stream()
                .map(ContactEntity.class::cast)
                .map(ContactEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Contact> findActiveByIdAndOwnerUserId(Long contactId, Long ownerUserId) {
        ContactEntity entity = ContactEntity.find(
                "id = ?1 and ownerUserId = ?2 and status = ?3",
                contactId,
                ownerUserId,
                IAgendaEntity.Status.ACTIVE
        ).firstResult();
        return Optional.ofNullable(entity).map(ContactEntity::toDomain);
    }

    @Override
    public Contact save(Contact contact) {
        ContactEntity entity = ContactEntity.fromDomain(contact);
        entity.createdAt = Objects.requireNonNullElse(entity.createdAt, OffsetDateTime.now());
        entity.status = Objects.requireNonNullElse(entity.status, IAgendaEntity.Status.ACTIVE);
        entity.persist();
        return entity.toDomain();
    }

    @Override
    public Optional<Contact> update(Contact contact) {
        ContactEntity entity = ContactEntity.find(
                "id = ?1 and ownerUserId = ?2 and status = ?3",
                contact.id,
                contact.ownerUserId,
                IAgendaEntity.Status.ACTIVE
        ).firstResult();
        if (entity == null) {
            return Optional.empty();
        }

        entity.firstName = contact.firstName;
        entity.lastName = contact.lastName;
        entity.birthDate = contact.birthDate;
        entity.relationshipDegree = contact.relationshipDegree;
        entity.updatedAt = contact.updatedAt;

        if (entity.phoneNumbers != null) {
            entity.phoneNumbers.forEach(phoneNumber -> {
                phoneNumber.status = IAgendaEntity.Status.DELETED;
                phoneNumber.updatedAt = contact.updatedAt;
            });
        }

        entity.phoneNumbers = contact.phoneNumbers.stream()
                .map(phoneNumber -> PhoneNumberEntity.fromDomain(phoneNumber, entity))
                .toList();

        return Optional.of(entity.toDomain());
    }

    @Override
    public void softDelete(Long contactId, Long ownerUserId, OffsetDateTime updatedAt) {
        ContactEntity entity = ContactEntity.find(
                "id = ?1 and ownerUserId = ?2 and status = ?3",
                contactId,
                ownerUserId,
                IAgendaEntity.Status.ACTIVE
        ).firstResult();
        if (entity == null) {
            return;
        }

        entity.status = IAgendaEntity.Status.DELETED;
        entity.updatedAt = updatedAt;

        if (entity.phoneNumbers != null) {
            entity.phoneNumbers.forEach(phoneNumber -> {
                phoneNumber.status = IAgendaEntity.Status.DELETED;
                phoneNumber.updatedAt = updatedAt;
            });
        }
    }
}
