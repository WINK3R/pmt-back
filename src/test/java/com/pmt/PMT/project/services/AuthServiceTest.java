package com.pmt.PMT.project.services;

import com.pmt.PMT.project.dto.*;
import com.pmt.PMT.project.models.User;
import com.pmt.PMT.project.repositories.UserRepository;
import com.pmt.PMT.project.security.AppUserDetails;
import com.pmt.PMT.project.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder encoder;
    @Mock private AuthenticationManager authManager;
    @Mock private JwtService jwt;

    @InjectMocks private AuthService authService;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("john@test.com");
        user.setUsername("john");
        user.setPasswordHash("hashed");
        user.setCreatedAt(Instant.now());
    }

    @Test
    void register_shouldSaveUser_whenValid() {
        RegisterRequest req = new RegisterRequest("john", "john@test.com", "pass");
        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(encoder.encode("pass")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(user);

        authService.register(req);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrow_whenEmailExists() {
        RegisterRequest req = new RegisterRequest("john", "john@test.com", "pass");
        when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(req));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void register_shouldThrow_whenUsernameExists() {
        RegisterRequest req = new RegisterRequest("john", "john@test.com", "pass");
        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("john")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(req));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void login_shouldReturnAuthResponse_whenValid() {
        LoginRequest req = new LoginRequest("john@test.com", "pass");
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(jwt.generateAccessToken(any(AppUserDetails.class))).thenReturn("jwt-token");

        AuthResponse res = authService.login(req);

        assertEquals("jwt-token", res.accessToken);
        assertEquals("john@test.com", res.email);
    }

    @Test
    void login_shouldThrow_whenEmailNotFound() {
        LoginRequest req = new LoginRequest("no@test.com", "pass");
        when(userRepository.findByEmail("no@test.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.login(req));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void login_shouldThrow_whenAuthenticationFails() {
        LoginRequest req = new LoginRequest("john@test.com", "bad");
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        doThrow(new BadCredentialsException("bad")).when(authManager).authenticate(any());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.login(req));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void getUserDataFromToken_shouldReturnMeResponse() {
        String token = "Bearer valid";
        when(jwt.extractUsername("valid")).thenReturn("john@test.com");
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));

        MeResponse res = authService.getUserDataFromToken(token);

        assertEquals("john@test.com", res.getEmail());
        assertEquals("john", res.getUsername());
    }

    @Test
    void getUserDataFromTokenWithoutBearer_shouldReturnMeResponse() {
        String token = "valid";
        when(jwt.extractUsername("valid")).thenReturn("john@test.com");
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));

        MeResponse res = authService.getUserDataFromToken(token);

        assertEquals("john@test.com", res.getEmail());
        assertEquals("john", res.getUsername());
    }

    @Test
    void getUserDataFromToken_shouldThrow_whenUsernameNull() {
        when(jwt.extractUsername("bad")).thenReturn(null);
        String token = "Bearer bad";

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.getUserDataFromToken(token));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void getUserDataFromToken_shouldThrow_whenUserNotFound() {
        when(jwt.extractUsername("valid")).thenReturn("ghost@test.com");
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        String token = "Bearer valid";
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.getUserDataFromToken(token));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
