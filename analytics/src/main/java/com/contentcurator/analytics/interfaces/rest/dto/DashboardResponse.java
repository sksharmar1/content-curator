package com.contentcurator.analytics.interfaces.rest.dto;

import java.util.List;
import java.util.Map;

public record DashboardResponse(
    int totalArticlesRead,
    int totalNotes,
    Integer predictedDaysToCert,
    Map<String, Long> categoryBreakdown,
    double averageSentiment,
    double averageUsefulness,
    List<ReadEventResponse> recentReads,
    List<NoteResponse> notes
) {}
