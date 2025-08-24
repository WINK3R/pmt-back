package com.pmt.PMT.project.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        String tag,
        LocalDate startDate,
        Instant createdAt,
        UserSummary createdBy
) {
}
