package com.contentcurator.ingestion.application.service;
import com.contentcurator.ingestion.application.port.out.ArticleRepository;
import com.contentcurator.ingestion.application.port.out.FeedRepository;
import com.contentcurator.ingestion.application.port.out.IngestionEventPublisher;
import com.contentcurator.ingestion.domain.model.Article;
import com.contentcurator.ingestion.domain.model.Feed;
import com.contentcurator.ingestion.infrastructure.rss.RssFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class FeedIngestionService {
    private final FeedRepository feedRepository;
    private final ArticleRepository articleRepository;
    private final RssFetcher rssFetcher;
    private final IngestionEventPublisher eventPublisher;
    @Scheduled(fixedDelayString = "${ingestion.poll-interval-ms:1800000}")
    public void pollAllFeeds() {
        List<Feed> feeds = feedRepository.findAllActive();
        log.info("Polling {} active feeds", feeds.size());
        feeds.forEach(this::pollFeed);
    }
    @Transactional
    public void pollFeed(Feed feed) {
        try {
            List<RssFetcher.RssItem> items = rssFetcher.fetch(feed.getUrl());
            int newCount = 0;
            for (RssFetcher.RssItem item : items) {
                String urlHash = sha256(item.url());
                if (articleRepository.existsByUrlHash(urlHash)) continue;
                Article article = Article.create(feed.getId(), item.title(), item.summary(), item.url(), urlHash, item.publishedAt());
                Article saved = articleRepository.save(article);
                saved.getDomainEvents().forEach(eventPublisher::publish);
                saved.clearDomainEvents();
                newCount++;
            }
            feed.markPolled();
            feedRepository.save(feed);
            log.info("Feed [{}] - {} new articles ingested", feed.getName(), newCount);
        } catch (Exception e) {
            log.error("Failed to poll feed {}: {}", feed.getUrl(), e.getMessage());
        }
    }
    @Transactional
    public Feed registerFeed(String name, String url, String category) {
        return feedRepository.save(Feed.create(name, url, category));
    }
    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(md.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { return input; }
    }
}
