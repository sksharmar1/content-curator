package com.contentcurator.recommendation.infrastructure.persistence;

import com.contentcurator.recommendation.application.port.out.UserInterestSnapshotRepository;
import com.contentcurator.recommendation.domain.model.UserInterestSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaUserInterestSnapshotRepository
        extends JpaRepository<UserInterestSnapshot, String>, UserInterestSnapshotRepository {

    Optional<UserInterestSnapshot> findByUserId(String userId);
}
