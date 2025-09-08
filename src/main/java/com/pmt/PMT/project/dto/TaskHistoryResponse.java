package com.pmt.PMT.project.dto;

import com.pmt.PMT.project.models.TaskHistory;
import java.time.Instant;
import java.util.UUID;

public record TaskHistoryResponse(
        UUID id,
        Instant changedAt,
        String field,
        HistoryValue oldValue,
        HistoryValue newValue,
        UserSummary changedBy
) {
    public TaskHistoryResponse(TaskHistory h, HistoryValue oldVal, HistoryValue newVal) {
        this(
                h.getId(),
                h.getChangedAt(),
                h.getField(),
                oldVal,
                newVal,
                h.getChangedBy() != null ? new UserSummary(h.getChangedBy()) : null
        );
    }
}
