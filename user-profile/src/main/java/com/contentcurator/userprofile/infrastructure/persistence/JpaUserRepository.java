package com.contentcurator.userprofile.infrastructure.persistence;
import com.contentcurator.userprofile.application.port.out.UserRepository;
import com.contentcurator.userprofile.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface JpaUserRepository extends JpaRepository<User, String>, UserRepository {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
