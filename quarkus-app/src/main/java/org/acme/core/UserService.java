package org.acme.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.User;

import java.time.OffsetDateTime;
import java.util.Optional;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @Transactional
    public User findOrCreateByExternalId(String externalId, String username, String email) {
        Optional<User> existing = userRepository.findByExternalId(externalId);
        if (existing.isPresent()) {
            return existing.get();
        }

        User u = new User(null, externalId, username, email, OffsetDateTime.now());
        return userRepository.save(u);
    }
}
