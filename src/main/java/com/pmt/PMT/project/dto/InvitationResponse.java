package com.pmt.PMT.project.dto;

import com.pmt.PMT.project.models.Invitation.Status;
import java.time.Instant;
import java.util.UUID;

public record InvitationResponse(
        UUID id,
        UUID projectId,
        String projectName,
        UserSummary invitedUser,
        UserSummary inviterUser,
        Status status,
        Instant createdAt,
        Instant acceptedAt
) {}
