package com.contentcurator.shared.events;
import com.contentcurator.shared.domain.DomainEvent;
import lombok.Getter;
@Getter
public class ContentIngestedEvent extends DomainEvent {
    private final String articleId;
    private final String feedId;
    private final String title;
    public ContentIngestedEvent(String articleId, String feedId, String title) {
        super("content.ingested");
        this.articleId = articleId;
        this.feedId = feedId;
        this.title = title;
    }
}
