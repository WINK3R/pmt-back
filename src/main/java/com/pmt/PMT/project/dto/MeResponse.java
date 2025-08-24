package com.pmt.PMT.project.dto;

import java.time.Instant;
import java.util.UUID;

public class MeResponse {
    private UUID id;
    private String username;
    private String email;
    private Instant createdAt;
    private String profileImageUrl;

    public MeResponse(UUID id, String username, String email, Instant createdAt, String profileImageUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
        this.profileImageUrl = profileImageUrl;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
