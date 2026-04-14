package com.contentcurator.shared.events;
import com.contentcurator.shared.domain.DomainEvent;
import lombok.Getter;
import java.util.List;
@Getter
public class UserInterestUpdatedEvent extends DomainEvent {
    private final String userId;
    private final List<String> topics;
    public UserInterestUpdatedEvent(String userId, List<String> topics) {
        super("user.interest.updated");
        this.userId = userId;
        this.topics = topics;
    }
}
