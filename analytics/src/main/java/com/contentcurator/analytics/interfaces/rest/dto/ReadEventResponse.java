package com.contentcurator.analytics.interfaces.rest.dto;

import java.time.Instant;

public record ReadEventResponse(
    String articleId,
    String articleTitle,
    String feedCategory,
    Instant readAt
) {}
