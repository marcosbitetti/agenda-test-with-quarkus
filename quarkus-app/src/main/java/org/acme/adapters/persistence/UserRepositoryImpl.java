package org.acme.adapters.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.core.UserRepository;
import org.acme.domain.User;

import java.util.Optional;
import java.util.Objects;

@ApplicationScoped
public class UserRepositoryImpl implements UserRepository {

    @Override
    public Optional<User> findByExternalId(String externalId) {
        UserEntity e = UserEntity.find("externalId", externalId).firstResult();
        return Optional.ofNullable(e).map(UserEntity::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity e = UserEntity.fromDomain(user);
        e.createdAt = Objects.requireNonNullElse(e.createdAt, java.time.OffsetDateTime.now());
        e.persist();
        return e.toDomain();
    }

    @Override
    public User update(User user) {
        UserEntity e = UserEntity.findById(user.id);
        if (e == null) return save(user);

        e.username = user.username;
        e.email = user.email;
        return e.toDomain();
    }
}
