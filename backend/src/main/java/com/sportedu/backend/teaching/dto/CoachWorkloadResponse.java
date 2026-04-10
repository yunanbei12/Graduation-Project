package com.sportedu.backend.teaching.dto;

public record CoachWorkloadResponse(
    Long coachId,
    long totalSchedules,
    long upcomingSchedules,
    long completedSchedules,
    long cancelledSchedules
) {
}
