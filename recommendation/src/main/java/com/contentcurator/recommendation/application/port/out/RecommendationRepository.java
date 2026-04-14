package com.contentcurator.recommendation.application.port.out;

import com.contentcurator.recommendation.domain.model.RecommendationScore;
import java.util.List;
import java.util.Optional;

public interface RecommendationRepository {
    RecommendationScore save(RecommendationScore score);
    List<RecommendationScore> findTopForUser(String userId, int limit);
    Optional<RecommendationScore> findByUserIdAndArticleId(String userId, String articleId);
    boolean existsByUserIdAndArticleId(String userId, String articleId);
}
