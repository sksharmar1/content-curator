package com.contentcurator.analytics.application.port.out;

import com.contentcurator.analytics.domain.model.UserProgress;
import java.util.Optional;

public interface UserProgressRepository {
    UserProgress save(UserProgress progress);
    Optional<UserProgress> findByUserId(String userId);
}
