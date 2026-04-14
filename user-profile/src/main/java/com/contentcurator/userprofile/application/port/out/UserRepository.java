package com.contentcurator.userprofile.application.port.out;
import com.contentcurator.userprofile.domain.model.User;
import java.util.Optional;
public interface UserRepository {
    User save(User user);
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
