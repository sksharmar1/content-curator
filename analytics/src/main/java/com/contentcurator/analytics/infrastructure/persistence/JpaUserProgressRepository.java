package com.contentcurator.analytics.infrastructure.persistence;

import com.contentcurator.analytics.application.port.out.UserProgressRepository;
import com.contentcurator.analytics.domain.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface JpaUserProgressRepository
        extends JpaRepository<UserProgress, String>, UserProgressRepository {
    Optional<UserProgress> findByUserId(String userId);
}
