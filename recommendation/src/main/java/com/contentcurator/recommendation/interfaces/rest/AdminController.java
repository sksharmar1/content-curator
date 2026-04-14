package com.contentcurator.recommendation.interfaces.rest;

import com.contentcurator.recommendation.application.service.RecommendationService;
import com.contentcurator.recommendation.application.port.out.UserInterestSnapshotRepository;
import com.contentcurator.recommendation.domain.model.UserInterestSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

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

    // Backfill scores for all existing articles against all user snapshots
    @PostMapping("/backfill-recommendations")
    public ResponseEntity<Map<String, Object>> backfill() {
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
