package org.acme.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import org.acme.domain.User;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @Transactional
    public User findOrCreateByExternalId(String externalId, String username, String email) {
        return userRepository.findByExternalId(externalId).map(currentUser -> {
            if (Objects.equals(currentUser.getUsername(), username) && Objects.equals(currentUser.getEmail(), email)) {
                return currentUser;
            }
            User updatedUser = new User(currentUser.getId(), currentUser.getExternalId(), username, email, currentUser.getCreatedAt());
            return userRepository.update(updatedUser);
        }).orElseGet(() -> {
            User u = new User(null, externalId, username, email, OffsetDateTime.now());
            try {
                return userRepository.save(u);
            } catch (PersistenceException e) {
                return userRepository.findByExternalId(externalId).orElseThrow(() -> e);
            }
        });
    }
}
