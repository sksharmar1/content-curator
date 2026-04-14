package com.contentcurator.ingestion.infrastructure.rss;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
@Component
@Slf4j
public class RssFetcher {
    public List<RssItem> fetch(String feedUrl) {
        try {
            SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(feedUrl)));
            return feed.getEntries().stream().map(this::toRssItem).filter(Objects::nonNull).toList();
        } catch (Exception e) {
            log.error("Failed to fetch RSS feed {}: {}", feedUrl, e.getMessage());
            return List.of();
        }
    }
    private RssItem toRssItem(SyndEntry entry) {
        if (entry.getLink() == null) return null;
        String summary = entry.getDescription() != null ? entry.getDescription().getValue() : "";
        summary = summary.replaceAll("<[^>]*>", "").trim();
        if (summary.length() > 500) summary = summary.substring(0, 500) + "...";
        Instant publishedAt = entry.getPublishedDate() != null ? entry.getPublishedDate().toInstant() : Instant.now();
        return new RssItem(entry.getTitle(), summary, entry.getLink(), publishedAt);
    }
    public record RssItem(String title, String summary, String url, Instant publishedAt) {}
}
