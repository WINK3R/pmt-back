package com.pmt.PMT.project.services;

import com.pmt.PMT.project.dto.*;
import com.pmt.PMT.project.models.User;
import com.pmt.PMT.project.repositories.UserRepository;
import com.pmt.PMT.project.security.AppUserDetails;
import com.pmt.PMT.project.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder,
                       AuthenticationManager am, JwtService jwt) {
        this.users = users; this.encoder = encoder; this.authManager = am; this.jwt = jwt;
    }

    public void register(RegisterRequest req) {
        if (users.existsByEmail(req.email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
        }
        if (users.existsByUsername(req.username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already in use");
        }

        User u = new User();
        u.setUsername(req.username);
        u.setEmail(req.email);
        u.setPasswordHash(encoder.encode(req.password)); // HASH ICI
        u.setCreatedAt(Instant.now());
        users.save(u);
    }

    public AuthResponse login(LoginRequest req) {
        var user = users.findByEmail(req.email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email, req.password)
            );
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        var ud = new AppUserDetails(user);
        var token = jwt.generateAccessToken(ud);

        var resp = new AuthResponse();
        resp.accessToken = token;
        resp.email = req.email;
        return resp;
    }

}
