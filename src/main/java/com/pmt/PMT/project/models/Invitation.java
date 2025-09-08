package com.pmt.PMT.project.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(
        name = "invitations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_invitation_project_invited",
                        columnNames = {"project_id", "invited_user_id"}),
                @UniqueConstraint(name = "uk_invitation_token", columnNames = {"token"})
        },
        indexes = {
                @Index(name = "idx_inv_project", columnList = "project_id"),
                @Index(name = "idx_inv_invited_user", columnList = "invited_user_id")
        }
)
public class Invitation {

    public enum Status { PENDING, ACCEPTED }

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_user_id", nullable = false)
    private User invited;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_user_id", nullable = false)
    private User inviter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    public Invitation() {}

}
