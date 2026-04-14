package com.contentcurator.ingestion.application.port.out;
import com.contentcurator.ingestion.domain.model.Article;
import java.util.Optional;
public interface ArticleRepository {
    Article save(Article article);
    Optional<Article> findByUrlHash(String urlHash);
    boolean existsByUrlHash(String urlHash);
}
