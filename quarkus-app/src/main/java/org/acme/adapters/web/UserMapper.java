package org.acme.adapters.web;

import org.acme.domain.User;

import java.util.Objects;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserDto toDto(final User user) {
        User source = Objects.requireNonNull(user, "user is required");
        return new UserDto(source.getId(), source.getExternalId(), source.getUsername(), source.getEmail());
    }
}