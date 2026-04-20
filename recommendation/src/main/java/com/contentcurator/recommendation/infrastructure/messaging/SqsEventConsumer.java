package com.contentcurator.recommendation.infrastructure.messaging;

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

    @Value("${aws.sqs.content-ingested-queue-url}")
    private String contentIngestedQueueUrl;

    @Value("${aws.sqs.user-activity-queue-url}")
    private String userActivityQueueUrl;

    @Scheduled(fixedDelay = 30000)
    public void pollContentIngested() {
        try {
            List<Message> messages = sqsClient.receiveMessage(
                ReceiveMessageRequest.builder()
                    .queueUrl(contentIngestedQueueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(1)
                    .build()
            ).messages();
            if (!messages.isEmpty()) {
                log.info("Received {} content-ingested events", messages.size());
            }
        } catch (Exception e) {
            log.debug("SQS unavailable, skipping poll: {}", e.getMessage());
        }
    }

    @Scheduled(fixedDelay = 30000)
    public void pollUserActivity() {
        try {
            List<Message> messages = sqsClient.receiveMessage(
                ReceiveMessageRequest.builder()
                    .queueUrl(userActivityQueueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(1)
                    .build()
            ).messages();
            if (!messages.isEmpty()) {
                log.info("Received {} user-activity events", messages.size());
            }
        } catch (Exception e) {
            log.debug("SQS unavailable, skipping poll: {}", e.getMessage());
        }
    }
}
