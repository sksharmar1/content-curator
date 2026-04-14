package com.contentcurator.recommendation.infrastructure.persistence;

import com.contentcurator.recommendation.application.port.out.RecommendationRepository;
import com.contentcurator.recommendation.domain.model.RecommendationScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaRecommendationRepository
        extends JpaRepository<RecommendationScore, String>, RecommendationRepository {

    @Query("SELECT r FROM RecommendationScore r WHERE r.userId = :userId " +
           "AND r.dismissed = false AND r.read = false " +
           "ORDER BY r.score DESC LIMIT :limit")
    List<RecommendationScore> findTopForUser(@Param("userId") String userId,
                                              @Param("limit") int limit);

    Optional<RecommendationScore> findByUserIdAndArticleId(String userId, String articleId);

    boolean existsByUserIdAndArticleId(String userId, String articleId);
}
