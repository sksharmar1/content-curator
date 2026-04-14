package com.contentcurator.recommendation.infrastructure.messaging;

import com.contentcurator.recommendation.application.service.RecommendationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqsEventConsumer {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final RecommendationService recommendationService;

    @Value("${aws.sqs.content-ingested-queue-url}")
    private String contentIngestedQueueUrl;

    @Value("${aws.sqs.user-activity-queue-url}")
    private String userActivityQueueUrl;

    @Scheduled(fixedDelay = 5000)
    public void pollContentIngested() {
        pollQueue(contentIngestedQueueUrl, message -> {
            try {
                handleContentIngested(message);
            } catch (Exception e) {
                log.error("Failed to handle content-ingested message {}: {}",
                    message.messageId(), e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    @Scheduled(fixedDelay = 5000)
    public void pollUserActivity() {
        pollQueue(userActivityQueueUrl, message -> {
            try {
                handleUserActivity(message);
            } catch (Exception e) {
                log.error("Failed to handle user-activity message {}: {}",
                    message.messageId(), e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    private void pollQueue(String queueUrl, java.util.function.Consumer<Message> handler) {
        try {
            ReceiveMessageResponse response = sqsClient.receiveMessage(
                ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(1)
                    .build());

            for (Message message : response.messages()) {
                try {
                    handler.accept(message);
                    sqsClient.deleteMessage(DeleteMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(message.receiptHandle())
                        .build());
                } catch (Exception e) {
                    log.error("Failed to process message {}: {}",
                        message.messageId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to poll queue {}: {}", queueUrl, e.getMessage());
        }
    }

    private void handleContentIngested(Message message) throws Exception {
        JsonNode node = objectMapper.readTree(message.body());
        String articleId    = node.get("articleId").asText();
        String title        = node.get("title").asText();
        String feedCategory = deriveCategoryFromTitle(title);
        log.info("Processing content-ingested event for article: {}", title);
        recommendationService.scoreArticleForAllUsers(articleId, title, feedCategory);
    }

    private void handleUserActivity(Message message) throws Exception {
        JsonNode node = objectMapper.readTree(message.body());
        String eventType = node.get("eventType").asText();
        if ("user.interest.updated".equals(eventType)) {
            String userId = node.get("userId").asText();
            List<String> topics = objectMapper.convertValue(
                node.get("topics"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            log.info("Processing user-interest-updated for user: {}", userId);
            recommendationService.updateUserInterests(userId, topics);
        }
    }

    private String deriveCategoryFromTitle(String title) {
        String lower = title.toLowerCase();
        if (lower.contains("machine learning") || lower.contains(" ai ") ||
            lower.contains("neural") || lower.contains("model")) return "AI/ML";
        if (lower.contains("aws") || lower.contains("cloud") ||
            lower.contains("lambda") || lower.contains("s3")) return "Cloud";
        if (lower.contains("spring") || lower.contains("java") ||
            lower.contains("kotlin")) return "Java";
        return "General";
    }
}
