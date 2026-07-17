package com.contentcurator.recommendation.interfaces.rest;

import com.contentcurator.recommendation.application.service.RecommendationService;
import com.contentcurator.recommendation.application.port.out.UserInterestSnapshotRepository;
import com.contentcurator.recommendation.domain.model.UserInterestSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final RecommendationService recommendationService;
    private final UserInterestSnapshotRepository snapshotRepository;
    private final JdbcTemplate jdbcTemplate;

    // Sync interest snapshots from the shared user_interests table.
    // The SQS user-activity pipeline is a stub, so snapshots must be
    // refreshed from the source of truth before scoring.
    @PostMapping("/sync-interests")
    public ResponseEntity<Map<String, Object>> syncInterests() {
        int synced = syncSnapshotsFromUserInterests();
        return ResponseEntity.ok(Map.of("usersSynced", synced, "status", "complete"));
    }

    private int syncSnapshotsFromUserInterests() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT user_id, topic FROM user_interests ORDER BY user_id");

        Map<String, List<String>> topicsByUser = new HashMap<>();
        for (Map<String, Object> row : rows) {
            topicsByUser
                .computeIfAbsent((String) row.get("user_id"), k -> new ArrayList<>())
                .add((String) row.get("topic"));
        }

        topicsByUser.forEach(recommendationService::updateUserInterests);
        log.info("Synced interest snapshots for {} users", topicsByUser.size());
        return topicsByUser.size();
    }

    // Backfill scores for all existing articles against all user snapshots
    @PostMapping("/backfill-recommendations")
    public ResponseEntity<Map<String, Object>> backfill() {
        // Refresh snapshots first so new users/interests are always scored
        syncSnapshotsFromUserInterests();

        List<Map<String, Object>> articles = jdbcTemplate.queryForList(
            "SELECT a.id, a.title, f.category " +
            "FROM articles a JOIN feeds f ON a.feed_id = f.id");

        List<UserInterestSnapshot> snapshots = snapshotRepository.findAll();

        log.info("Backfilling {} articles for {} users", articles.size(), snapshots.size());

        int scored = 0;
        for (Map<String, Object> article : articles) {
            String articleId    = (String) article.get("id");
            String title        = (String) article.get("title");
            String feedCategory = (String) article.get("category");

            recommendationService.scoreArticleForAllUsers(articleId, title, feedCategory);
            scored++;
        }

        return ResponseEntity.ok(Map.of(
            "articlesProcessed", scored,
            "usersScored", snapshots.size(),
            "status", "complete"
        ));
    }
}
