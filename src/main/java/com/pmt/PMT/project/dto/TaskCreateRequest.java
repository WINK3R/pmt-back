package com.pmt.PMT.project.dto;

import com.pmt.PMT.project.models.Task;
import java.time.Instant;
import java.util.UUID;

public record TaskCreateRequest(
        String title,
        String description,
        String label,
        Instant dueDate,
        Task.Priority priority,
        Task.Status status,
        UUID projectId,
        UUID assigneeId,
        UUID createdById,
        UUID updatedById,
        Instant createdAt,
        Instant updatedAt
) {}
