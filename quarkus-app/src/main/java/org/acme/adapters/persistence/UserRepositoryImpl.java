package org.acme.adapters.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.core.UserRepository;
import org.acme.domain.User;

import java.util.Optional;

@ApplicationScoped
public class UserRepositoryImpl implements UserRepository {

    @Override
    public Optional<User> findByExternalId(String externalId) {
        UserEntity e = UserEntity.find("externalId", externalId).firstResult();
        if (e == null) return Optional.empty();
        return Optional.of(e.toDomain());
    }

    @Override
    public User save(User user) {
        UserEntity e = UserEntity.fromDomain(user);
        if (e.createdAt == null) e.createdAt = java.time.OffsetDateTime.now();
        e.persist();
        return e.toDomain();
    }

    @Override
    public User update(User user) {
        UserEntity e = UserEntity.findById(user.id);
        if (e == null) {
            return save(user);
        }

        e.username = user.username;
        e.email = user.email;
        return e.toDomain();
    }
}
