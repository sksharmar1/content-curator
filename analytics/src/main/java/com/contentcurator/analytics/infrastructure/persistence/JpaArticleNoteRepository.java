package com.contentcurator.analytics.infrastructure.persistence;

import com.contentcurator.analytics.application.port.out.ArticleNoteRepository;
import com.contentcurator.analytics.domain.model.ArticleNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaArticleNoteRepository
        extends JpaRepository<ArticleNote, String>, ArticleNoteRepository {
    List<ArticleNote> findByUserId(String userId);
    Optional<ArticleNote> findByUserIdAndArticleId(String userId, String articleId);

    @Query("SELECT n FROM ArticleNote n WHERE n.sentimentScore IS NULL")
    List<ArticleNote> findUnanalyzed();
}
