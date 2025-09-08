package com.pmt.PMT.project.dto;

import java.util.UUID;

public record ProjectMinimalRequest (
        UUID id,
        String name,
        String description,
        String tag
) {}
