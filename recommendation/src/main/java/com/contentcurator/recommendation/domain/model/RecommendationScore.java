package com.contentcurator.recommendation.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "recommendation_scores",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "article_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendationScore {

    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String articleId;

    @Column(nullable = false)
    private String articleTitle;

    @Column(nullable = false)
    private String feedCategory;

    // 0.0 to 1.0 — higher = more relevant to user interests
    @Column(nullable = false)
    private double score;

    private boolean dismissed;
    private boolean read;

    @Column(nullable = false)
    private Instant scoredAt;

    public static RecommendationScore create(String userId, String articleId,
                                              String articleTitle, String feedCategory,
                                              double score) {
        RecommendationScore rs = new RecommendationScore();
        rs.id = UUID.randomUUID().toString();
        rs.userId = userId;
        rs.articleId = articleId;
        rs.articleTitle = articleTitle;
        rs.feedCategory = feedCategory;
        rs.score = score;
        rs.dismissed = false;
        rs.read = false;
        rs.scoredAt = Instant.now();
        return rs;
    }

    public void markRead() { this.read = true; }
    public void dismiss() { this.dismissed = true; }
}
