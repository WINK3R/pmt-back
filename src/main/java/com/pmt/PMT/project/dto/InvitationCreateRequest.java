package com.pmt.PMT.project.dto;

import java.util.UUID;

public record InvitationCreateRequest(
        UUID projectId,
        String emailInvited
) {}
