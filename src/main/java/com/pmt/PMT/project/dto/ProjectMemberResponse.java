package com.pmt.PMT.project.dto;

import com.pmt.PMT.project.models.ProjectMembership;
import java.time.Instant;
import java.util.UUID;

public record ProjectMemberResponse(
        UUID membershipId,
        ProjectMembership.Role role,
        Instant joinedAt,
        UserSummary user
) {}