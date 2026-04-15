package com.contentcurator.userprofile.infrastructure.messaging;

import com.contentcurator.userprofile.application.port.out.UserRepository;
import com.contentcurator.userprofile.domain.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DigestScheduler {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Value("${aws.sqs.digest-scheduled-queue-url}")
    private String digestQueueUrl;

    // Fire every Sunday at 8am UTC — "0 0 8 ? * SUN"
    // For testing use fixedDelay instead
    @Scheduled(cron = "${digest.schedule:0 0 8 ? * SUN}")
    public void scheduleWeeklyDigests() {
        log.info("Scheduling weekly digest for all users");

        List<User> users = userRepository.findAll();

        for (User user : users) {
            try {
                Map<String, String> event = Map.of(
                    "eventType",   "digest.scheduled",
                    "userId",      user.getId(),
                    "email",       user.getEmail(),
                    "displayName", user.getDisplayName()
                );

                String payload = objectMapper.writeValueAsString(event);
                sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(digestQueueUrl)
                    .messageBody(payload)
                    .build());

                log.info("Digest scheduled for user: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to schedule digest for {}: {}",
                    user.getEmail(), e.getMessage());
            }
        }
    }
}
