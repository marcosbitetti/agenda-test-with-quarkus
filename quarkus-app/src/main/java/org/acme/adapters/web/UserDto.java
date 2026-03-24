package org.acme.adapters.web;

import org.acme.domain.User;

public class UserDto {
    public Long id;
    public String externalId;
    public String username;
    public String email;

    public UserDto() {}

    public UserDto(User u) {
        this.id = u.id;
        this.externalId = u.externalId;
        this.username = u.username;
        this.email = u.email;
    }
}
