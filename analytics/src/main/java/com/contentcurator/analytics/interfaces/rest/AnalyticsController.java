package com.contentcurator.analytics.interfaces.rest;

import com.contentcurator.analytics.application.service.AnalyticsService;
import com.contentcurator.analytics.domain.model.ArticleNote;
import com.contentcurator.analytics.interfaces.rest.dto.DashboardResponse;
import com.contentcurator.analytics.interfaces.rest.dto.NoteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // Record that user read an article
    @PostMapping("/read")
    public ResponseEntity<Void> recordRead(
            Authentication auth,
            @RequestBody Map<String, String> body) {
        analyticsService.recordRead(
            auth.getName(),
            body.get("articleId"),
            body.get("articleTitle"),
            body.getOrDefault("feedCategory", "General"));
        return ResponseEntity.ok().build();
    }

    // Save a note with automatic sentiment analysis
    @PostMapping("/notes")
    public ResponseEntity<NoteResponse> saveNote(
            Authentication auth,
            @RequestBody Map<String, String> body) {
        ArticleNote note = analyticsService.saveNote(
            auth.getName(),
            body.get("articleId"),
            body.get("content"));
        return ResponseEntity.ok(new NoteResponse(
            note.getId(), note.getArticleId(), note.getContent(),
            note.getSentimentScore(), note.getSentimentLabel(),
            note.getUsefulnessScore(), note.getCreatedAt()));
    }

    // Full progress dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(Authentication auth) {
        return ResponseEntity.ok(analyticsService.getDashboard(auth.getName()));
    }
}
