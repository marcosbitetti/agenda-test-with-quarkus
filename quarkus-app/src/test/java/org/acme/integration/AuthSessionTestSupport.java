package org.acme.integration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.adapters.persistence.AuthSessionEntity;
import org.acme.adapters.persistence.ContactEntity;
import org.acme.adapters.persistence.PhoneNumberEntity;
import org.acme.core.AuthSessionService;
import org.acme.domain.IAgendaEntity;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class AuthSessionTestSupport {

    @Inject
    AuthSessionService authSessionService;

    @Transactional
    public void expireAccessToken(String sessionId, Instant expiresAt) {
        AuthSessionEntity entity = AuthSessionEntity.findById(sessionId);
        if (entity != null) {
            entity.accessTokenExpiresAt = expiresAt;
        }
    }

    @Transactional
    public void expireRefreshToken(String sessionId, Instant expiresAt) {
        AuthSessionEntity entity = AuthSessionEntity.findById(sessionId);
        if (entity != null) {
            entity.refreshTokenExpiresAt = expiresAt;
        }
    }

    public long cleanupExpiredSessions() {
        return authSessionService.cleanupExpiredSessions();
    }

    @Transactional
    public void clearContacts() {
        PhoneNumberEntity.deleteAll();
        ContactEntity.deleteAll();
    }

    @Transactional
    public List<String> findActivePhoneNumbers(Long contactId) {
        ContactEntity entity = ContactEntity.findById(contactId);
        if (entity == null || entity.phoneNumbers == null) {
            return List.of();
        }

        return entity.phoneNumbers.stream().filter(phoneNumber -> phoneNumber.status == IAgendaEntity.Status.ACTIVE)
                .map(phoneNumber -> phoneNumber.number).toList();
    }
}
