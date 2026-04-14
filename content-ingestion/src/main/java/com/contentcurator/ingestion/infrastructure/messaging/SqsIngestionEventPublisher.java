package com.contentcurator.ingestion.infrastructure.messaging;
import com.contentcurator.ingestion.application.port.out.IngestionEventPublisher;
import com.contentcurator.shared.domain.DomainEvent;
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
public class SqsIngestionEventPublisher implements IngestionEventPublisher {
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    @Value("${aws.sqs.content-ingested-queue-url}") private String contentIngestedQueueUrl;
    @Override
    public void publish(DomainEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(contentIngestedQueueUrl)
                .messageBody(payload)
                .build());
            log.info("Published event {} to SQS", event.getEventType());
        } catch (Exception e) {
            log.error("Failed to publish event {}: {}", event.getEventType(), e.getMessage());
        }
    }
}
