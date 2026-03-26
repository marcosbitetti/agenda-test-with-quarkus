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
public final class ContactRepositoryImpl implements ContactRepository {

    @Override
    public List<Contact> listActiveByOwnerUserId(final Long ownerUserId) {
        var results = ContactEntity.find("ownerUserId = ?1 and status = ?2", ownerUserId, IAgendaEntity.Status.ACTIVE)
                .list();

        return results.stream().map(ContactEntity.class::cast).map(ContactEntity::toDomain).toList();
    }

    @Override
    public Optional<Contact> findActiveByIdAndOwnerUserId(final Long contactId, final Long ownerUserId) {
        ContactEntity entity = ContactEntity.find("id = ?1 and ownerUserId = ?2 and status = ?3", contactId,
                ownerUserId, IAgendaEntity.Status.ACTIVE).firstResult();
        return Optional.ofNullable(entity).map(ContactEntity::toDomain);
    }

    @Override
    public Contact save(final Contact contact) {
        ContactEntity entity = ContactEntity.fromDomain(contact);
        entity.createdAt = Objects.requireNonNullElse(entity.createdAt, OffsetDateTime.now());
        entity.status = Objects.requireNonNullElse(entity.status, IAgendaEntity.Status.ACTIVE);
        entity.persist();
        return entity.toDomain();
    }

    @Override
    public Optional<Contact> update(final Contact contact) {
        ContactEntity entity = ContactEntity.find("id = ?1 and ownerUserId = ?2 and status = ?3", contact.getId(),
                contact.getOwnerUserId(), IAgendaEntity.Status.ACTIVE).firstResult();
        if (entity == null) {
            return Optional.empty();
        }

        entity.firstName = contact.getFirstName();
        entity.lastName = contact.getLastName();
        entity.birthDate = contact.getBirthDate();
        entity.relationshipDegree = contact.getRelationshipDegree();
        entity.updatedAt = contact.getUpdatedAt();

        if (entity.phoneNumbers != null) {
            entity.phoneNumbers.forEach(phoneNumber -> {
                phoneNumber.status = IAgendaEntity.Status.DELETED;
                phoneNumber.updatedAt = contact.getUpdatedAt();
            });
        }

        entity.phoneNumbers = contact.getPhoneNumbers().stream()
                .map(phoneNumber -> PhoneNumberEntity.fromDomain(phoneNumber, entity)).toList();

        return Optional.of(entity.toDomain());
    }

    @Override
    public void softDelete(final Long contactId, final Long ownerUserId, final OffsetDateTime updatedAt) {
        ContactEntity entity = ContactEntity.find("id = ?1 and ownerUserId = ?2 and status = ?3", contactId,
                ownerUserId, IAgendaEntity.Status.ACTIVE).firstResult();
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
