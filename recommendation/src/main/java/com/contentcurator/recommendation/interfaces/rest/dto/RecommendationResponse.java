package com.contentcurator.recommendation.interfaces.rest.dto;

import java.time.Instant;

public record RecommendationResponse(
    String articleId,
    String articleTitle,
    String feedCategory,
    double score,
    Instant scoredAt
) {}
