package org.acme.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import org.acme.domain.User;

import java.time.OffsetDateTime;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @Transactional
    public User findOrCreateByExternalId(String externalId, String username, String email) {
        return userRepository.findByExternalId(externalId).map(currentUser -> {
            if (currentUser.hasIdentity(username, email)) {
                return currentUser;
            }
            return userRepository.update(currentUser.withIdentity(username, email));
        }).orElseGet(() -> {
            User u = User.newUser(externalId, username, email, OffsetDateTime.now());
            try {
                return userRepository.save(u);
            } catch (PersistenceException e) {
                return userRepository.findByExternalId(externalId).orElseThrow(() -> e);
            }
        });
    }
}
