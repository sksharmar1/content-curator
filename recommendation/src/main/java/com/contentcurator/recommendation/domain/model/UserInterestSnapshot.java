package com.contentcurator.recommendation.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// Local read-model — updated via SQS user-activity events
// Avoids cross-service DB joins
@Entity
@Table(name = "user_interest_snapshots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInterestSnapshot {

    @Id
    private String userId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "snapshot_topics",
                     joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "topic")
    private List<String> topics = new ArrayList<>();

    @Column(nullable = false)
    private Instant updatedAt;

    public static UserInterestSnapshot create(String userId, List<String> topics) {
        UserInterestSnapshot s = new UserInterestSnapshot();
        s.userId = userId;
        s.topics = new ArrayList<>(topics);
        s.updatedAt = Instant.now();
        return s;
    }

    public void updateTopics(List<String> newTopics) {
        this.topics = new ArrayList<>(newTopics);
        this.updatedAt = Instant.now();
    }
}
