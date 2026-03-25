package org.acme.core;

import org.acme.domain.Contact;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ContactRepository {
    List<Contact> listActiveByOwnerUserId(Long ownerUserId);

    Optional<Contact> findActiveByIdAndOwnerUserId(Long contactId, Long ownerUserId);

    Contact save(Contact contact);

    Optional<Contact> update(Contact contact);

    void softDelete(Long contactId, Long ownerUserId, OffsetDateTime updatedAt);
}