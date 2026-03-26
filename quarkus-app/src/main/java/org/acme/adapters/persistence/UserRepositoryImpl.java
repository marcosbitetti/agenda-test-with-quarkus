package org.acme.adapters.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.core.UserRepository;
import org.acme.domain.User;

import java.util.Optional;
import java.util.Objects;

@ApplicationScoped
public final class UserRepositoryImpl implements UserRepository {

    @Override
    public Optional<User> findByExternalId(final String externalId) {
        UserEntity e = UserEntity.find("externalId", externalId).firstResult();
        return Optional.ofNullable(e).map(UserEntity::toDomain);
    }

    @Override
    public User save(final User user) {
        UserEntity e = UserEntity.fromDomain(user);
        e.createdAt = Objects.requireNonNullElse(e.createdAt, java.time.OffsetDateTime.now());
        e.persist();
        return e.toDomain();
    }

    @Override
    public User update(final User user) {
        UserEntity e = UserEntity.findById(user.getId());
        if (e == null) {
            return save(user);
        }

        e.username = user.getUsername();
        e.email = user.getEmail();
        return e.toDomain();
    }
}
