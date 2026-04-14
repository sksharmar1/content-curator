package com.contentcurator.analytics.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "read_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadEvent {

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

    @Column(nullable = false)
    private Instant readAt;

    public static ReadEvent create(String userId, String articleId,
                                    String articleTitle, String feedCategory) {
        ReadEvent e = new ReadEvent();
        e.id = UUID.randomUUID().toString();
        e.userId = userId;
        e.articleId = articleId;
        e.articleTitle = articleTitle;
        e.feedCategory = feedCategory;
        e.readAt = Instant.now();
        return e;
    }
}
