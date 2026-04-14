package com.contentcurator.analytics.application.service;

import com.contentcurator.analytics.application.port.out.*;
import com.contentcurator.analytics.domain.model.*;
import com.contentcurator.analytics.infrastructure.client.MlServiceClient;
import com.contentcurator.analytics.interfaces.rest.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final ReadEventRepository readEventRepository;
    private final ArticleNoteRepository noteRepository;
    private final UserProgressRepository progressRepository;
    private final MlServiceClient mlServiceClient;

    @Transactional
    public void recordRead(String userId, String articleId,
                            String articleTitle, String feedCategory) {
        try {
            ReadEvent event = ReadEvent.create(userId, articleId, articleTitle, feedCategory);
            readEventRepository.save(event);

            UserProgress progress = progressRepository.findByUserId(userId)
                .orElseGet(() -> UserProgress.create(userId));
            progress.incrementReads(feedCategory);
            progressRepository.save(progress);

            log.info("Recorded read for user [{}]: {}", userId, articleTitle);
        } catch (Exception e) {
            log.error("Failed to record read: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public ArticleNote saveNote(String userId, String articleId, String content) {
        try {
            log.info("Saving note for user [{}] article [{}]", userId, articleId);

            // Always create fresh note
            ArticleNote note = ArticleNote.create(userId, articleId, content);

            // Run sentiment analysis immediately (fallback handles ML service down)
            try {
                MlServiceClient.SentimentResult result =
                    mlServiceClient.analyzeSentiment(content);
                note.applySentiment(result.score(), result.label(), result.usefulnessScore());
                log.info("Sentiment applied: {} ({})", result.label(), result.score());
            } catch (Exception e) {
                log.warn("Sentiment analysis failed, saving without: {}", e.getMessage());
            }

            ArticleNote saved = noteRepository.save(note);

            // Update progress
            UserProgress progress = progressRepository.findByUserId(userId)
                .orElseGet(() -> UserProgress.create(userId));
            progress.incrementNotes();
            progressRepository.save(progress);

            log.info("Note saved with id [{}]", saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Failed to save note: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Scheduled(fixedDelay = 120000)
    public void retryUnanalyzedNotes() {
        List<ArticleNote> unanalyzed = noteRepository.findUnanalyzed();
        if (!unanalyzed.isEmpty()) {
            log.info("Retrying sentiment for {} notes", unanalyzed.size());
            unanalyzed.forEach(note -> {
                try {
                    MlServiceClient.SentimentResult result =
                        mlServiceClient.analyzeSentiment(note.getContent());
                    note.applySentiment(result.score(), result.label(), result.usefulnessScore());
                    noteRepository.save(note);
                } catch (Exception e) {
                    log.warn("Retry failed for note [{}]: {}", note.getId(), e.getMessage());
                }
            });
        }
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String userId) {
        try {
            log.info("Getting dashboard for user [{}]", userId);

            UserProgress progress = progressRepository.findByUserId(userId)
                .orElseGet(() -> UserProgress.create(userId));

            List<ReadEvent> recentReads = readEventRepository.findByUserId(userId)
                .stream()
                .sorted((a, b) -> b.getReadAt().compareTo(a.getReadAt()))
                .limit(10)
                .toList();

            List<ArticleNote> notes = noteRepository.findByUserId(userId);

            Map<String, Long> categoryBreakdown = recentReads.stream()
                .collect(Collectors.groupingBy(ReadEvent::getFeedCategory,
                         Collectors.counting()));

            double avgSentiment = notes.stream()
                .filter(n -> n.getSentimentScore() != null)
                .mapToDouble(ArticleNote::getSentimentScore)
                .average().orElse(0.0);

            double avgUsefulness = notes.stream()
                .filter(n -> n.getUsefulnessScore() != null)
                .mapToDouble(ArticleNote::getUsefulnessScore)
                .average().orElse(0.0);

            List<NoteResponse> noteResponses = notes.stream()
                .map(n -> new NoteResponse(
                    n.getId(), n.getArticleId(), n.getContent(),
                    n.getSentimentScore(), n.getSentimentLabel(),
                    n.getUsefulnessScore(), n.getCreatedAt()))
                .toList();

            List<ReadEventResponse> readResponses = recentReads.stream()
                .map(r -> new ReadEventResponse(
                    r.getArticleId(), r.getArticleTitle(),
                    r.getFeedCategory(), r.getReadAt()))
                .toList();

            return new DashboardResponse(
                progress.getTotalArticlesRead(),
                progress.getTotalNotes(),
                progress.getPredictedDaysToCert(),
                categoryBreakdown,
                avgSentiment,
                avgUsefulness,
                readResponses,
                noteResponses
            );
        } catch (Exception e) {
            log.error("Failed to get dashboard: {}", e.getMessage(), e);
            throw e;
        }
    }
}
