package com.sportedu.backend.teaching.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ScheduleResponse(
    Long scheduleId,
    Long courseId,
    String courseName,
    Long coachId,
    String coachName,
    LocalDate scheduleDate,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String location,
    Integer capacity,
    Integer minGroupCount,
    Integer enrolledCount,
    Integer waitlistCount,
    Integer status,
    String cancelReason
) {
}
