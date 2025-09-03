package com.pmt.PMT.project.mappers;

import com.pmt.PMT.project.dto.ProjectMemberResponse;
import com.pmt.PMT.project.models.ProjectMembership;
import com.pmt.PMT.project.models.User;

public final class ProjectMembershipMapper {

    private ProjectMembershipMapper() {}

    public static ProjectMemberResponse toResponse(ProjectMembership membership) {
        User user = membership.getUser();

        return new ProjectMemberResponse(
                membership.getId(),
                membership.getRole(),
                membership.getJoinedAt(),
                UserMapper.toSummary(user)
        );
    }
}
