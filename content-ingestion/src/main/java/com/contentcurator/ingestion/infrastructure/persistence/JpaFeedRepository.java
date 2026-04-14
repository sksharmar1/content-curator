package com.contentcurator.ingestion.infrastructure.persistence;
import com.contentcurator.ingestion.application.port.out.FeedRepository;
import com.contentcurator.ingestion.domain.model.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface JpaFeedRepository extends JpaRepository<Feed, String>, FeedRepository {
    List<Feed> findAllByActiveTrue();
    default List<Feed> findAllActive() { return findAllByActiveTrue(); }
}
