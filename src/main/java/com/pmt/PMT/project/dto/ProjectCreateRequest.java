package com.pmt.PMT.project.dto;

import java.time.LocalDate;

public record ProjectCreateRequest(
        String name,
        String description,
        LocalDate startDate,
        String tag
) {}