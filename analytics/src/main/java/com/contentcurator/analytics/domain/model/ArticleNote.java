package com.contentcurator.analytics.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "article_notes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleNote {

    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String articleId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // Sentiment score from Python ML service: -1.0 (negative) to 1.0 (positive)
    private Double sentimentScore;

    // Human-readable label: POSITIVE, NEGATIVE, NEUTRAL
    private String sentimentLabel;

    // Usefulness score extracted from note e.g. "90% useful" -> 0.9
    private Double usefulnessScore;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant analyzedAt;

    public static ArticleNote create(String userId, String articleId, String content) {
        ArticleNote n = new ArticleNote();
        n.id = UUID.randomUUID().toString();
        n.userId = userId;
        n.articleId = articleId;
        n.content = content;
        n.createdAt = Instant.now();
        return n;
    }

    public void applySentiment(double score, String label, Double usefulnessScore) {
        this.sentimentScore = score;
        this.sentimentLabel = label;
        this.usefulnessScore = usefulnessScore;
        this.analyzedAt = Instant.now();
    }
}
