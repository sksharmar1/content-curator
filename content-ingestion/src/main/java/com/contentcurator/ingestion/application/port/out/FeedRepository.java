package com.contentcurator.ingestion.application.port.out;
import com.contentcurator.ingestion.domain.model.Feed;
import java.util.List;
public interface FeedRepository {
    Feed save(Feed feed);
    List<Feed> findAllActive();
}
