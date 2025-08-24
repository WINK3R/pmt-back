package com.pmt.PMT.project.models;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"username"}),
                @UniqueConstraint(columnNames = {"email"})
        }
)
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @JsonIgnore
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "profile_image_url", length = 512)
    private String profileImageUrl;

    @JsonIgnore
    @Column(name = "profile_image_key", length = 512)
    private String profileImageKey;

    public User() {}

    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            profileImageUrl = "/static/avatars/default.png";
            profileImageKey = "avatars/default.png";
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getProfileImageKey() { return  profileImageKey; }
    public void setProfileImageKey(String profileImageKey) { this.profileImageKey = profileImageKey; }

    public void setProfileImageUrl(String imageUrl) { this.profileImageUrl = imageUrl; }
    public String getProfileImageUrl() { return profileImageUrl; }
}
