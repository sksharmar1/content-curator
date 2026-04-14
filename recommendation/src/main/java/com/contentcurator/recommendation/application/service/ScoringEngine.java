package com.contentcurator.recommendation.application.service;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ScoringEngine {

    // Score an article against a user's interest topics.
    // Returns 0.0 (no match) to 1.0 (strong match).
    // Algorithm: keyword presence in title + category match,
    // normalised by number of topics so more interests = fairer scoring.
    public double score(String articleTitle, String feedCategory, List<String> userTopics) {
        if (userTopics == null || userTopics.isEmpty()) return 0.0;

        String titleLower    = articleTitle.toLowerCase();
        String categoryLower = feedCategory.toLowerCase();

        double totalScore = 0.0;

        for (String topic : userTopics) {
            String topicLower = topic.toLowerCase();
            double topicScore = 0.0;

            // Direct category match — strong signal
            if (categoryLower.contains(topicLower) || topicLower.contains(categoryLower)) {
                topicScore += 0.5;
            }

            // Title contains full topic phrase
            if (titleLower.contains(topicLower)) {
                topicScore += 0.4;
            } else {
                // Partial match — any word in the topic appears in the title
                String[] words = topicLower.split("\\s+");
                long matchedWords = java.util.Arrays.stream(words)
                    .filter(w -> w.length() > 3) // skip short words like "and", "the"
                    .filter(titleLower::contains)
                    .count();
                if (matchedWords > 0) {
                    topicScore += 0.2 * ((double) matchedWords / words.length);
                }
            }

            totalScore += Math.min(topicScore, 1.0);
        }

        // Normalise: average across all topics, capped at 1.0
        return Math.min(totalScore / userTopics.size(), 1.0);
    }
}
