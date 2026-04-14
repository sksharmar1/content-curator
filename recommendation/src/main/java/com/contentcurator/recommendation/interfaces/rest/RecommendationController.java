package com.contentcurator.recommendation.interfaces.rest;

import com.contentcurator.recommendation.application.service.RecommendationService;
import com.contentcurator.recommendation.interfaces.rest.dto.RecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<List<RecommendationResponse>> getRecommendations(
            Authentication auth,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(
            recommendationService.getTopRecommendations(auth.getName(), limit));
    }

    @PostMapping("/{articleId}/read")
    public ResponseEntity<Void> markRead(Authentication auth,
                                          @PathVariable String articleId) {
        recommendationService.markRead(auth.getName(), articleId);
        return ResponseEntity.ok().build();
    }
}
