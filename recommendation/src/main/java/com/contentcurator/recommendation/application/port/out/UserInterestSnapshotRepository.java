package com.contentcurator.recommendation.application.port.out;

import com.contentcurator.recommendation.domain.model.UserInterestSnapshot;
import java.util.List;
import java.util.Optional;

public interface UserInterestSnapshotRepository {
    UserInterestSnapshot save(UserInterestSnapshot snapshot);
    Optional<UserInterestSnapshot> findByUserId(String userId);
    List<UserInterestSnapshot> findAll();
}
