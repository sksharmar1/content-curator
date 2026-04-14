package com.contentcurator.analytics.interfaces.rest.dto;

import java.time.Instant;

public record NoteResponse(
    String id,
    String articleId,
    String content,
    Double sentimentScore,
    String sentimentLabel,
    Double usefulnessScore,
    Instant createdAt
) {}
