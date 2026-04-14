package com.contentcurator.ingestion.application.port.out;
import com.contentcurator.shared.domain.DomainEvent;
public interface IngestionEventPublisher {
    void publish(DomainEvent event);
}
