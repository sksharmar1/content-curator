package com.contentcurator.analytics.application.port.out;

import com.contentcurator.analytics.domain.model.ArticleNote;
import java.util.List;
import java.util.Optional;

public interface ArticleNoteRepository {
    ArticleNote save(ArticleNote note);
    List<ArticleNote> findByUserId(String userId);
    Optional<ArticleNote> findByUserIdAndArticleId(String userId, String articleId);
    List<ArticleNote> findUnanalyzed();
}
