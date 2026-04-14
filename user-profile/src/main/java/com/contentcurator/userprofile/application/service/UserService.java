package com.contentcurator.userprofile.application.service;
import com.contentcurator.shared.exception.DomainException;
import com.contentcurator.userprofile.application.dto.RegisterUserCommand;
import com.contentcurator.userprofile.application.dto.UserResponse;
import com.contentcurator.userprofile.application.port.out.EventPublisher;
import com.contentcurator.userprofile.application.port.out.UserRepository;
import com.contentcurator.userprofile.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventPublisher eventPublisher;
    @Transactional
    public UserResponse register(RegisterUserCommand cmd) {
        if (userRepository.existsByEmail(cmd.email()))
            throw new DomainException("Email already registered: " + cmd.email());
        User user = User.create(cmd.email(), passwordEncoder.encode(cmd.password()), cmd.displayName());
        User saved = userRepository.save(user);
        saved.getDomainEvents().forEach(eventPublisher::publish);
        saved.clearDomainEvents();
        log.info("Registered new user: {}", saved.getId());
        return toResponse(saved);
    }
    @Transactional
    public void addInterest(String userId, String topic) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new DomainException("User not found: " + userId));
        user.addInterest(topic);
        User saved = userRepository.save(user);
        saved.getDomainEvents().forEach(eventPublisher::publish);
        saved.clearDomainEvents();
    }
    @Transactional(readOnly = true)
    public UserResponse getUser(String userId) {
        return userRepository.findById(userId)
            .map(this::toResponse)
            .orElseThrow(() -> new DomainException("User not found: " + userId));
    }
    private UserResponse toResponse(User user) {
        List<String> topics = user.getInterests().stream().map(i -> i.getTopic()).toList();
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), topics, user.getCreatedAt());
    }
}
