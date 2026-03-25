package org.acme.adapters.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.core.ContactRepository;
import org.acme.domain.Contact;
import org.acme.domain.IAgendaEntity;

import java.time.OffsetDateTime;
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
        return entity == null ? Optional.empty() : Optional.of(entity.toDomain());
    }

    @Override
    public Contact save(Contact contact) {
        ContactEntity entity = ContactEntity.fromDomain(contact);
        if (entity.createdAt == null) {
            entity.createdAt = OffsetDateTime.now();
        }
        if (entity.status == null) {
            entity.status = IAgendaEntity.Status.ACTIVE;
        }
        entity.persist();
        return entity.toDomain();
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
            for (PhoneNumberEntity phoneNumber : entity.phoneNumbers) {
                phoneNumber.status = IAgendaEntity.Status.DELETED;
                phoneNumber.updatedAt = updatedAt;
            }
        }
    }
}