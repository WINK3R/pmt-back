package com.pmt.PMT.project.mappers;

import com.pmt.PMT.project.dto.InvitationResponse;
import com.pmt.PMT.project.dto.UserSummary;
import com.pmt.PMT.project.models.Invitation;
import com.pmt.PMT.project.models.Project;

import java.util.UUID;

public final class InvitationMapper {
    private InvitationMapper() {}

    public static InvitationResponse toResponse(Invitation inv) {
        UUID projectId = inv.getProject() != null ? inv.getProject().getId() : null;
        Project project = inv.getProject() != null ? inv.getProject() : null;
        UserSummary invitedSummary = null;
        if (inv.getInvited() != null) {
            var u = inv.getInvited();
            invitedSummary = new UserSummary(
                    u.getId(),
                    u.getUsername(),
                    u.getProfileImageUrl()
            );
        }

        UserSummary inviterSummary = null;
        if (inv.getInviter() != null) {
            var u = inv.getInviter();
            inviterSummary = new UserSummary(
                    u.getId(),
                    u.getUsername(),
                    u.getProfileImageUrl()
            );
        }

        assert project != null;
        return new InvitationResponse(
                inv.getId(),
                projectId,
                project.getName(),
                invitedSummary,
                inviterSummary,
                inv.getStatus(),
                inv.getCreatedAt(),
                inv.getAcceptedAt()
        );
    }
}
