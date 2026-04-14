package com.contentcurator.userprofile.domain.model;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interest {
    private String topic;
    private int weight;
    public Interest(String topic) { this(topic, 1); }
    public Interest(String topic, int weight) {
        if (topic == null || topic.isBlank()) throw new IllegalArgumentException("Topic cannot be blank");
        if (weight < 1 || weight > 10) throw new IllegalArgumentException("Weight must be 1-10");
        this.topic = topic.toLowerCase().trim();
        this.weight = weight;
    }
    public Interest incrementWeight() { return new Interest(this.topic, Math.min(this.weight + 1, 10)); }
}
