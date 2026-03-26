package org.acme.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.Contact;
import org.acme.domain.IAgendaEntity;
import org.acme.i18n.AgendaMessages;
import org.acme.i18n.MessageKey;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
public class ContactService {

    @Inject
    ContactRepository contactRepository;

    public List<Contact> listActiveByOwnerUserId(Long ownerUserId) {
        return contactRepository.listActiveByOwnerUserId(ownerUserId);
    }

    public Optional<Contact> findActiveByIdAndOwnerUserId(Long contactId, Long ownerUserId) {
        return contactRepository.findActiveByIdAndOwnerUserId(contactId, ownerUserId);
    }

    @Transactional
    public Contact create(Long ownerUserId, ContactWriteInput input) {
        OffsetDateTime now = OffsetDateTime.now();
        Contact contact = Contact.builder().id(null)
                .ownerUserId(Objects.requireNonNull(ownerUserId, AgendaMessages.get(MessageKey.OWNER_USER_REQUIRED)))
                .firstName(input.firstName()).lastName(input.lastName()).birthDate(input.birthDate())
                .phoneNumbers(Contact.createActivePhoneNumbers(input.phoneNumbers(), now))
                .relationshipDegree(input.relationshipDegree())
                .createdAt(now).updatedAt(now).status(IAgendaEntity.Status.ACTIVE).build();
        return contactRepository.save(contact);
    }

    @Transactional
    public Optional<Contact> update(Long ownerUserId, Long contactId, ContactWriteInput input) {
        Objects.requireNonNull(ownerUserId, AgendaMessages.get(MessageKey.OWNER_USER_REQUIRED));
        Objects.requireNonNull(contactId, AgendaMessages.get(MessageKey.CONTACT_REQUIRED));

        return contactRepository.findActiveByIdAndOwnerUserId(contactId, ownerUserId).flatMap(current -> {
            OffsetDateTime now = OffsetDateTime.now();
            Contact updated = Contact.builder().id(current.getId()).ownerUserId(current.getOwnerUserId())
                    .firstName(input.firstName()).lastName(input.lastName()).birthDate(input.birthDate())
                    .phoneNumbers(Contact.createActivePhoneNumbers(input.phoneNumbers(), now))
                    .relationshipDegree(input.relationshipDegree())
                    .createdAt(current.getCreatedAt()).updatedAt(now).status(current.getStatus()).build();
            return contactRepository.update(updated);
        });
    }

    @Transactional
    public void softDelete(Long contactId, Long ownerUserId) {
        contactRepository.softDelete(contactId, ownerUserId, OffsetDateTime.now());
    }
}
