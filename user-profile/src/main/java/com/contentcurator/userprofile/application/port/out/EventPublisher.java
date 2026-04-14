package com.contentcurator.userprofile.application.port.out;
import com.contentcurator.shared.domain.DomainEvent;
public interface EventPublisher {
    void publish(DomainEvent event);
}
