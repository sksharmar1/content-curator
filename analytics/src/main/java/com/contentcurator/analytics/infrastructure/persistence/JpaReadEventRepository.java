package com.contentcurator.analytics.infrastructure.persistence;

import com.contentcurator.analytics.application.port.out.ReadEventRepository;
import com.contentcurator.analytics.domain.model.ReadEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JpaReadEventRepository
        extends JpaRepository<ReadEvent, String>, ReadEventRepository {
    List<ReadEvent> findByUserId(String userId);
    long countByUserId(String userId);
}
