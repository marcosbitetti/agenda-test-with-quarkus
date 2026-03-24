package org.acme.core;

import org.acme.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @Test
    public void findExistingByExternalId() {
        User existing = new User(1L, "ext-123", "joao", "joao@example.com", OffsetDateTime.now());
        when(userRepository.findByExternalId("ext-123")).thenReturn(Optional.of(existing));

        User result = userService.findOrCreateByExternalId("ext-123", "joao", "joao@example.com");

        assertNotNull(result);
        assertEquals(1L, result.id);
        assertEquals("ext-123", result.externalId);
        assertEquals("joao", result.username);
    }

    @Test
    public void createNewWhenNotFound() {
        when(userRepository.findByExternalId("ext-new")).thenReturn(Optional.empty());

        User saved = new User(2L, "ext-new", "newuser", "new@example.com", OffsetDateTime.now());
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = userService.findOrCreateByExternalId("ext-new", "newuser", "new@example.com");

        assertNotNull(result);
        assertEquals(2L, result.id);
        assertEquals("ext-new", result.externalId);
        assertEquals("newuser", result.username);
    }
}
