package com.pmt.PMT.project.events;

import java.util.UUID;

public record TaskAssigneeChangedEvent(
        UUID taskId,
        String taskTitle,
        String actorDisplayName,
        String newAssigneeEmail,
        String newAssigneeName
) {}
