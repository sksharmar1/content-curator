package com.contentcurator.userprofile.infrastructure.messaging;
import com.contentcurator.shared.domain.DomainEvent;
import com.contentcurator.userprofile.application.port.out.EventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
@Component
@RequiredArgsConstructor
@Slf4j
public class SqsEventPublisher implements EventPublisher {
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    @Value("${aws.sqs.user-activity-queue-url}")
    private String userActivityQueueUrl;
    @Override
    public void publish(DomainEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(userActivityQueueUrl)
                .messageBody(payload)
                .build());
            log.info("Published event {} to SQS", event.getEventType());
        } catch (Exception e) {
            log.error("Failed to publish event {}: {}", event.getEventType(), e.getMessage());
        }
    }
}
