package com.pmt.PMT.project.dto;

import com.pmt.PMT.project.models.TaskHistory;
import java.time.Instant;
import java.util.UUID;

public record TaskHistoryResponse(
        UUID id,
        Instant changedAt,
        String field,
        String oldValue,
        String newValue,
        UserSummary changedBy
) {
    public TaskHistoryResponse(TaskHistory h) {
        this(
                h.getId(),
                h.getChangedAt(),
                h.getField(),
                h.getOldValue(),
                h.getNewValue(),
                h.getChangedBy() != null ? new UserSummary(h.getChangedBy()) : null
        );
    }
}
