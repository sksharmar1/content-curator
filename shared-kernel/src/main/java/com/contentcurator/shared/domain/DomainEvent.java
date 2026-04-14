package com.contentcurator.shared.domain;
import java.time.Instant;
import java.util.UUID;
public abstract class DomainEvent {
    private final String eventId = UUID.randomUUID().toString();
    private final Instant occurredOn = Instant.now();
    private final String eventType;
    protected DomainEvent(String eventType) { this.eventType = eventType; }
    public String getEventId() { return eventId; }
    public Instant getOccurredOn() { return occurredOn; }
    public String getEventType() { return eventType; }
}
