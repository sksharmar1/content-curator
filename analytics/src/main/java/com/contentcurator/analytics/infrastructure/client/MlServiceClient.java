package com.contentcurator.analytics.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class MlServiceClient {

    @Value("${ml.service.url:http://localhost:5000}")
    private String mlServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public SentimentResult analyzeSentiment(String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request =
                new HttpEntity<>(Map.of("text", text), headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                mlServiceUrl + "/analyze/sentiment", request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map body = response.getBody();
                double score = ((Number) body.get("score")).doubleValue();
                String label = (String) body.get("label");
                Double usefulness = extractUsefulnessScore(text);
                return new SentimentResult(score, label, usefulness);
            }
        } catch (Exception e) {
            log.warn("ML service unavailable, using fallback: {}", e.getMessage());
        }
        // Fallback — simple rule-based when ML service is down
        return fallbackSentiment(text);
    }

    private SentimentResult fallbackSentiment(String text) {
        String lower = text.toLowerCase();
        double score;
        String label;

        long positiveWords = java.util.Arrays.stream(
            new String[]{"great","useful","helpful","excellent","good",
                         "amazing","perfect","love","best","clear"})
            .filter(lower::contains).count();

        long negativeWords = java.util.Arrays.stream(
            new String[]{"bad","confusing","unclear","difficult","wrong",
                         "terrible","useless","poor","hard","boring"})
            .filter(lower::contains).count();

        if (positiveWords > negativeWords) {
            score = 0.3 + (0.1 * positiveWords);
            label = "POSITIVE";
        } else if (negativeWords > positiveWords) {
            score = -0.3 - (0.1 * negativeWords);
            label = "NEGATIVE";
        } else {
            score = 0.0;
            label = "NEUTRAL";
        }

        return new SentimentResult(
            Math.max(-1.0, Math.min(1.0, score)),
            label,
            extractUsefulnessScore(text));
    }

    private Double extractUsefulnessScore(String text) {
        // Extract patterns like "90% useful", "8/10", "7 out of 10"
        Pattern pct = Pattern.compile("(\\d+)%\\s*useful");
        Matcher m = pct.matcher(text.toLowerCase());
        if (m.find()) return Double.parseDouble(m.group(1)) / 100.0;

        Pattern outOf = Pattern.compile("(\\d+)\\s*(?:out of|/)\\s*10");
        m = outOf.matcher(text.toLowerCase());
        if (m.find()) return Double.parseDouble(m.group(1)) / 10.0;

        return null;
    }

    public record SentimentResult(double score, String label, Double usefulnessScore) {}
}
