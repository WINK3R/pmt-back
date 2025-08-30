package com.pmt.PMT.project.mappers;

import com.pmt.PMT.project.dto.UserSummary;
import com.pmt.PMT.project.models.User;

public final class UserMapper {
    private UserMapper() {}

    public static UserSummary toSummary(User u) {
        if (u == null) return null;
        return new UserSummary(
                u.getId(),
                u.getUsername(),
                u.getProfileImageUrl()
        );
    }
}
