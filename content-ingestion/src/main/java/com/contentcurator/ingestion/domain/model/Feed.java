package com.contentcurator.ingestion.domain.model;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;
@Entity
@Table(name = "feeds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feed {
    @Id private String id;
    @Column(nullable = false) private String name;
    @Column(nullable = false, unique = true) private String url;
    @Column(nullable = false) private String category;
    private Instant lastPolledAt;
    @Column(nullable = false) private boolean active = true;
    public static Feed create(String name, String url, String category) {
        Feed f = new Feed();
        f.id = UUID.randomUUID().toString();
        f.name = name; f.url = url; f.category = category;
        return f;
    }
    public void markPolled() { this.lastPolledAt = Instant.now(); }
}
