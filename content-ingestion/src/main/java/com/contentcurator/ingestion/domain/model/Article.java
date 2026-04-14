package com.contentcurator.ingestion.domain.model;
import com.contentcurator.shared.domain.AggregateRoot;
import com.contentcurator.shared.events.ContentIngestedEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "articles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends AggregateRoot {
    @Id private String id;
    @Column(nullable = false) private String feedId;
    @Column(nullable = false) private String title;
    @Column(columnDefinition = "TEXT") private String summary;
    @Column(nullable = false) private String url;
    @Column(nullable = false, unique = true) private String urlHash;
    @ElementCollection
    @CollectionTable(name = "article_tags", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();
    @Enumerated(EnumType.STRING) private ArticleStatus status = ArticleStatus.NEW;
    private Instant publishedAt;
    private Instant ingestedAt;
    public static Article create(String feedId, String title, String summary, String url, String urlHash, Instant publishedAt) {
        Article a = new Article();
        a.id = ArticleId.generate().value();
        a.feedId = feedId; a.title = title; a.summary = summary;
        a.url = url; a.urlHash = urlHash; a.publishedAt = publishedAt;
        a.ingestedAt = Instant.now();
        a.registerEvent(new ContentIngestedEvent(a.id, feedId, title));
        return a;
    }
    public void tag(List<String> newTags) { this.tags.clear(); this.tags.addAll(newTags); }
    public enum ArticleStatus { NEW, PROCESSED, FAILED }
}
