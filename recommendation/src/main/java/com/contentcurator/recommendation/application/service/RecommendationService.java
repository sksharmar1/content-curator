package com.contentcurator.recommendation.application.service;

import com.contentcurator.recommendation.application.port.out.RecommendationRepository;
import com.contentcurator.recommendation.application.port.out.UserInterestSnapshotRepository;
import com.contentcurator.recommendation.domain.model.RecommendationScore;
import com.contentcurator.recommendation.domain.model.UserInterestSnapshot;
import com.contentcurator.recommendation.interfaces.rest.dto.RecommendationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserInterestSnapshotRepository snapshotRepository;
    private final ScoringEngine scoringEngine;

    // Called by SQS consumer when a new article arrives
    @Transactional
    public void scoreArticleForAllUsers(String articleId, String articleTitle, String feedCategory) {
        List<UserInterestSnapshot> allSnapshots = snapshotRepository.findAll();

        for (UserInterestSnapshot snapshot : allSnapshots) {
            // Skip if already scored for this user
            if (recommendationRepository.existsByUserIdAndArticleId(snapshot.getUserId(), articleId)) {
                continue;
            }

            double score = scoringEngine.score(articleTitle, feedCategory, snapshot.getTopics());

            // Only store if there is meaningful relevance (above noise floor)
            if (score > 0.05) {
                RecommendationScore rs = RecommendationScore.create(
                    snapshot.getUserId(), articleId, articleTitle, feedCategory, score);
                recommendationRepository.save(rs);
                log.debug("Scored article [{}] for user [{}]: {}", articleTitle, snapshot.getUserId(), score);
            }
        }
    }

    // Called by SQS consumer when user interests change
    @Transactional
    public void updateUserInterests(String userId, List<String> topics) {
        UserInterestSnapshot snapshot = snapshotRepository.findByUserId(userId)
            .orElseGet(() -> UserInterestSnapshot.create(userId, topics));
        snapshot.updateTopics(topics);
        snapshotRepository.save(snapshot);
        log.info("Updated interest snapshot for user [{}]: {}", userId, topics);
    }

    // REST endpoint — get top recommendations for a user
    @Transactional(readOnly = true)
    public List<RecommendationResponse> getTopRecommendations(String userId, int limit) {
        return recommendationRepository.findTopForUser(userId, limit)
            .stream()
            .map(rs -> new RecommendationResponse(
                rs.getArticleId(),
                rs.getArticleTitle(),
                rs.getFeedCategory(),
                rs.getScore(),
                rs.getScoredAt()))
            .toList();
    }

    @Transactional
    public void markRead(String userId, String articleId) {
        recommendationRepository.findByUserIdAndArticleId(userId, articleId)
            .ifPresent(rs -> {
                rs.markRead();
                recommendationRepository.save(rs);
            });
    }
}
