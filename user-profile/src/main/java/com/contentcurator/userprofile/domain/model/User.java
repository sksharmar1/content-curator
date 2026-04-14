package com.contentcurator.userprofile.domain.model;
import com.contentcurator.shared.domain.AggregateRoot;
import com.contentcurator.shared.events.UserInterestUpdatedEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends AggregateRoot {
    @Id private String id;
    @Column(nullable = false, unique = true) private String email;
    @Column(nullable = false) private String passwordHash;
    @Column(nullable = false) private String displayName;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_interests", joinColumns = @JoinColumn(name = "user_id"))
    private List<Interest> interests = new ArrayList<>();
    @Enumerated(EnumType.STRING) private UserRole role = UserRole.USER;
    @Column(nullable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;
    public static User create(String email, String passwordHash, String displayName) {
        User user = new User();
        user.id = UserId.generate().value();
        user.email = email.toLowerCase().trim();
        user.passwordHash = passwordHash;
        user.displayName = displayName;
        user.createdAt = Instant.now();
        user.updatedAt = Instant.now();
        return user;
    }
    public void addInterest(String topic) {
        boolean exists = interests.stream().anyMatch(i -> i.getTopic().equalsIgnoreCase(topic));
        if (!exists) {
            interests.add(new Interest(topic));
            updatedAt = Instant.now();
            registerEvent(new UserInterestUpdatedEvent(this.id, interests.stream().map(Interest::getTopic).toList()));
        }
    }
    public void reinforceInterest(String topic) {
        interests = interests.stream()
            .map(i -> i.getTopic().equalsIgnoreCase(topic) ? i.incrementWeight() : i)
            .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        updatedAt = Instant.now();
    }
    public List<Interest> getInterests() { return Collections.unmodifiableList(interests); }
    public enum UserRole { USER, ADMIN }
}
