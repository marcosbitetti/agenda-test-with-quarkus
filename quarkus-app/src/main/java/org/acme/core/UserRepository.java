package org.acme.core;

import org.acme.domain.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByExternalId(String externalId);

    User save(User user);

    User update(User user);
}
