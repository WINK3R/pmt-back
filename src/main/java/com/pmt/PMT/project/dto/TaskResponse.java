package com.pmt.PMT.project.dto;

import com.pmt.PMT.project.models.Task;

import java.time.Instant;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        Instant dueDate,
        Task.Priority priority,
        Task.Status status,
        Instant createdAt,
        Instant updatedAt,
        UserSummary createdBy,
        UserSummary updatedBy,
        UserSummary assignee,
        UUID projectId,
        String label
) {

    public TaskResponse(Task t) {
        this(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getDueDate(),
                t.getPriority(),
                t.getStatus(),
                t.getCreatedAt(),
                t.getUpdatedAt(),
                t.getCreatedBy() != null ? new UserSummary(t.getCreatedBy()) : null,
                t.getUpdatedBy() != null ? new UserSummary(t.getUpdatedBy()) : null,
                t.getAssignee() != null ? new UserSummary(t.getAssignee()) : null,
                t.getProject() != null ? t.getProject().getId() : null,
                t.getLabel()
        );
    }
}
