package com.contentcurator.ingestion.infrastructure.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import java.net.URI;
@Configuration
public class AwsConfig {
    @Value("${aws.region:us-east-1}") private String region;
    @Value("${aws.endpoint-override:}") private String endpointOverride;
    @Value("${aws.access-key:test}") private String accessKey;
    @Value("${aws.secret-key:test}") private String secretKey;
    @Bean
    public SqsClient sqsClient() {
        var builder = SqsClient.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)));
        if (!endpointOverride.isBlank())
            builder.endpointOverride(URI.create(endpointOverride));
        return builder.build();
    }
}
