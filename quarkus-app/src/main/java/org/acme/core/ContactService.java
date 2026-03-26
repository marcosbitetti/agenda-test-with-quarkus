package org.acme.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.Contact;
import org.acme.domain.IAgendaEntity;
import org.acme.domain.PhoneNumber;
import org.acme.i18n.AgendaMessages;
import org.acme.i18n.MessageKey;

import java.time.LocalDate;
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
    public Contact create(Long ownerUserId, String firstName, String lastName, LocalDate birthDate,
            List<String> phoneNumbers, String relationshipDegree) {
        OffsetDateTime now = OffsetDateTime.now();
        Contact contact = Contact.builder().id(null)
                .ownerUserId(Objects.requireNonNull(ownerUserId, AgendaMessages.get(MessageKey.OWNER_USER_REQUIRED)))
                .firstName(firstName).lastName(lastName).birthDate(birthDate)
                .phoneNumbers(buildPhoneNumbers(phoneNumbers, now)).relationshipDegree(relationshipDegree)
                .createdAt(now).updatedAt(now).status(IAgendaEntity.Status.ACTIVE).build();
        return contactRepository.save(contact);
    }

    @Transactional
    public Optional<Contact> update(Long ownerUserId, Long contactId, String firstName, String lastName,
            LocalDate birthDate, List<String> phoneNumbers, String relationshipDegree) {
        Objects.requireNonNull(ownerUserId, AgendaMessages.get(MessageKey.OWNER_USER_REQUIRED));
        Objects.requireNonNull(contactId, AgendaMessages.get(MessageKey.CONTACT_REQUIRED));

        return contactRepository.findActiveByIdAndOwnerUserId(contactId, ownerUserId).flatMap(current -> {
            OffsetDateTime now = OffsetDateTime.now();
            Contact updated = Contact.builder().id(current.getId()).ownerUserId(current.getOwnerUserId())
                    .firstName(firstName).lastName(lastName).birthDate(birthDate)
                    .phoneNumbers(buildPhoneNumbers(phoneNumbers, now)).relationshipDegree(relationshipDegree)
                    .createdAt(current.getCreatedAt()).updatedAt(now).status(current.getStatus()).build();
            return contactRepository.update(updated);
        });
    }

    @Transactional
    public void softDelete(Long contactId, Long ownerUserId) {
        contactRepository.softDelete(contactId, ownerUserId, OffsetDateTime.now());
    }

    private List<PhoneNumber> buildPhoneNumbers(List<String> phoneNumbers, OffsetDateTime now) {
        Objects.requireNonNull(phoneNumbers, AgendaMessages.get(MessageKey.PHONE_REQUIRED));
        return phoneNumbers.stream().map(number -> new PhoneNumber(null, number, now, now, IAgendaEntity.Status.ACTIVE))
                .toList();
    }
}
