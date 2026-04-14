package com.contentcurator.ingestion.infrastructure.persistence;
import com.contentcurator.ingestion.application.port.out.ArticleRepository;
import com.contentcurator.ingestion.domain.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface JpaArticleRepository extends JpaRepository<Article, String>, ArticleRepository {
    Optional<Article> findByUrlHash(String urlHash);
    boolean existsByUrlHash(String urlHash);
}
