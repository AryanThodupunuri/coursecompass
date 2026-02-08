package com.coursecompass.api.dto;

import java.util.List;

public record AnalyzeResponse(
    String professorName,
    String courseId,
    Double avgRating,
    String sentimentSummary,
    List<String> redditTopThreads
) {}
