package org.acme.adapters.web;

import org.acme.domain.User;

public class UserDto {
    public Long id;
    public String externalId;
    public String username;
    public String email;

    public UserDto() {
    }

    public UserDto(User u) {
        this.id = u.getId();
        this.externalId = u.getExternalId();
        this.username = u.getUsername();
        this.email = u.getEmail();
    }
}
