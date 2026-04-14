package com.contentcurator.ingestion.domain.model;
import java.util.UUID;
public record ArticleId(String value) {
    public static ArticleId generate() { return new ArticleId(UUID.randomUUID().toString()); }
}
