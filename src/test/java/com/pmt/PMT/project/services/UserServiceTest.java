package com.pmt.PMT.project.services;

import com.pmt.PMT.project.models.User;
import com.pmt.PMT.project.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("Alice");
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));

        List<User> users = userService.findAll();

        assertThat(users).hasSize(1);
        assertThat(users.getFirst().getUsername()).isEqualTo("Alice");
        verify(userRepository).findAll();
    }

    @Test
    void create_shouldSaveAndReturnUser() {
        when(userRepository.save(user)).thenReturn(user);

        User saved = userService.create(user);

        assertThat(saved.getUsername()).isEqualTo("Alice");
        verify(userRepository).save(user);
    }

    @Test
    void getById_shouldReturnUserIfExists() {
        UUID id = user.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User found = userService.getById(id);

        assertThat(found).isEqualTo(user);
        verify(userRepository).findById(id);
    }

    @Test
    void getById_shouldThrowIfNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> userService.getById(id));

        verify(userRepository).findById(id);
    }
}
