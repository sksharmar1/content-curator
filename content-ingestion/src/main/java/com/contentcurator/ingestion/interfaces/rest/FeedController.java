package com.contentcurator.ingestion.interfaces.rest;
import com.contentcurator.ingestion.application.service.FeedIngestionService;
import com.contentcurator.ingestion.domain.model.Feed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {
    private final FeedIngestionService feedIngestionService;
    @PostMapping
    public ResponseEntity<Feed> registerFeed(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(feedIngestionService.registerFeed(body.get("name"), body.get("url"), body.get("category")));
    }
    @PostMapping("/poll")
    public ResponseEntity<Void> triggerPoll() {
        feedIngestionService.pollAllFeeds();
        return ResponseEntity.accepted().build();
    }
}
