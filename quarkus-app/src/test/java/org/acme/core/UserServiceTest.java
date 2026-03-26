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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
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
        assertEquals(1L, result.getId());
        assertEquals("ext-123", result.getExternalId());
        assertEquals("joao", result.getUsername());
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    public void createNewWhenNotFound() {
        when(userRepository.findByExternalId("ext-new")).thenReturn(Optional.empty());

        User saved = new User(2L, "ext-new", "newuser", "new@example.com", OffsetDateTime.now());
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = userService.findOrCreateByExternalId("ext-new", "newuser", "new@example.com");

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("ext-new", result.getExternalId());
        assertEquals("newuser", result.getUsername());
    }

    @Test
    public void updateExistingWhenIdentityDataChanges() {
        User existing = new User(1L, "ext-123", "joao", "joao@old.example.com", OffsetDateTime.now());
        User updated = new User(1L, "ext-123", "joao", "joao@example.com", existing.getCreatedAt());

        when(userRepository.findByExternalId("ext-123")).thenReturn(Optional.of(existing));
        when(userRepository.update(any(User.class))).thenReturn(updated);

        User result = userService.findOrCreateByExternalId("ext-123", "joao", "joao@example.com");

        assertNotNull(result);
        assertEquals("joao@example.com", result.getEmail());
        verify(userRepository).update(any(User.class));
    }
}
