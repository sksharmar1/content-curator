package com.contentcurator.ingestion.infrastructure.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3ArticleStore {

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;

    @Value("${aws.s3.articles-bucket:content-curator-articles}")
    private String articlesBucket;

    public void storeArticle(String articleId, String title,
                              String summary, String url, String category) {
        try {
            Map<String, String> article = Map.of(
                "articleId", articleId,
                "title",     title,
                "summary",   summary,
                "url",       url,
                "category",  category
            );
            String json  = objectMapper.writeValueAsString(article);
            String s3Key = "articles/" + category + "/" + articleId + ".json";

            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(articlesBucket)
                    .key(s3Key)
                    .contentType("application/json")
                    .build(),
                RequestBody.fromString(json));

            log.debug("Stored article [{}] to S3: {}", articleId, s3Key);
        } catch (Exception e) {
            log.warn("Failed to store article [{}] to S3: {}", articleId, e.getMessage());
        }
    }
}
