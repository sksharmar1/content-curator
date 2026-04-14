package com.contentcurator.analytics.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;

// Aggregate read-model — updated on every read/note event
@Entity
@Table(name = "user_progress")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProgress {

    @Id
    private String userId;

    private int totalArticlesRead;
    private int totalNotes;

    // Per-category read counts stored as JSON string for simplicity
    @Column(columnDefinition = "TEXT")
    private String categoryCountsJson;

    // ML-predicted days to cert completion (set by Python ML service)
    private Integer predictedDaysToCert;

    @Column(nullable = false)
    private Instant lastUpdated;

    public static UserProgress create(String userId) {
        UserProgress p = new UserProgress();
        p.userId = userId;
        p.totalArticlesRead = 0;
        p.totalNotes = 0;
        p.categoryCountsJson = "{}";
        p.lastUpdated = Instant.now();
        return p;
    }

    public void incrementReads(String category) {
        this.totalArticlesRead++;
        this.lastUpdated = Instant.now();
        // Update category counts
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, Integer> counts = mapper.readValue(
                categoryCountsJson,
                mapper.getTypeFactory().constructMapType(
                    java.util.HashMap.class, String.class, Integer.class));
            counts.merge(category, 1, Integer::sum);
            this.categoryCountsJson = mapper.writeValueAsString(counts);
        } catch (Exception ignored) {}
    }

    public void incrementNotes() {
        this.totalNotes++;
        this.lastUpdated = Instant.now();
    }

    public void updatePrediction(int days) {
        this.predictedDaysToCert = days;
        this.lastUpdated = Instant.now();
    }
}
