package org.acme.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.Contact;
import org.acme.domain.IAgendaEntity;
import org.acme.domain.PhoneNumber;

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
    public Contact create(Long ownerUserId,
                          String firstName,
                          String lastName,
                          LocalDate birthDate,
                          List<String> phoneNumbers,
                          String relationshipDegree) {
        OffsetDateTime now = OffsetDateTime.now();
        Contact contact = new Contact(
                null,
                Objects.requireNonNull(ownerUserId, "Usuario dono obrigatorio."),
                firstName,
                lastName,
                birthDate,
                buildPhoneNumbers(phoneNumbers, now),
                relationshipDegree,
                now,
                now,
                IAgendaEntity.Status.ACTIVE
        );
        return contactRepository.save(contact);
    }

    @Transactional
    public void softDelete(Long contactId, Long ownerUserId) {
        contactRepository.softDelete(contactId, ownerUserId, OffsetDateTime.now());
    }

    private List<PhoneNumber> buildPhoneNumbers(List<String> phoneNumbers, OffsetDateTime now) {
        Objects.requireNonNull(phoneNumbers, "Telefone obrigatorio.");
        return phoneNumbers.stream()
                .map(number -> new PhoneNumber(null, number, now, now, IAgendaEntity.Status.ACTIVE))
                .toList();
    }
}