package com.contentcurator.analytics.application.port.out;

import com.contentcurator.analytics.domain.model.ReadEvent;
import java.util.List;

public interface ReadEventRepository {
    ReadEvent save(ReadEvent event);
    List<ReadEvent> findByUserId(String userId);
    long countByUserId(String userId);
}
