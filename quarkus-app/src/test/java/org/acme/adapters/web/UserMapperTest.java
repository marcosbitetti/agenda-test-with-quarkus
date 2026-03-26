package org.acme.adapters.web;

import org.acme.domain.User;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserMapperTest {

    @Test
    public void mapUserToDto() {
        User user = new User(1L, "ext-123", "joao", "joao@example.com", OffsetDateTime.parse("2026-03-25T10:15:30Z"));

        UserDto dto = UserMapper.toDto(user);

        assertEquals(1L, dto.id());
        assertEquals("ext-123", dto.externalId());
        assertEquals("joao", dto.username());
        assertEquals("joao@example.com", dto.email());
    }

    @Test
    public void rejectNullUserSource() {
        assertThrows(NullPointerException.class, () -> UserMapper.toDto(null));
    }
}