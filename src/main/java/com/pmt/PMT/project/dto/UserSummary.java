package com.pmt.PMT.project.dto;

import com.pmt.PMT.project.models.User;

import java.util.UUID;

public record UserSummary(UUID id, String username, String profileImageUrl) {
    public UserSummary(User user) {
        this(user.getId(), user.getUsername(), user.getProfileImageUrl());
    }
}
