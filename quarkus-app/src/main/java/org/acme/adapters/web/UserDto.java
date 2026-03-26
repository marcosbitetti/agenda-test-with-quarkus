package org.acme.adapters.web;

public record UserDto(Long id, String externalId, String username, String email) {
}
