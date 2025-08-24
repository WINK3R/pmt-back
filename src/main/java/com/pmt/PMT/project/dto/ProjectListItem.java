package com.pmt.PMT.project.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectListItem(UUID id, String name, String description, String tag, LocalDate startDate,
                              Instant createdAt, UserSummary createdBy, long openTasks, long completedTasks) {

}
