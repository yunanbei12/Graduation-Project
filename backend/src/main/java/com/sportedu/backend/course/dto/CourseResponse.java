package com.sportedu.backend.course.dto;

import java.math.BigDecimal;
import java.util.List;

public record CourseResponse(
    Long courseId,
    String courseName,
    String courseCode,
    Integer courseType,
    String sportType,
    String coverUrl,
    String description,
    List<String> detailImages,
    BigDecimal price,
    BigDecimal originalPrice,
    Integer lessonCount,
    Integer validityDays,
    Integer isDoorToDoor,
    List<String> serviceAreas,
    String fixedScheduleDesc,
    String fixedLocation,
    Integer maxParticipants,
    Integer groupSuccessCount,
    Integer operationWeight,
    Integer status
) {
}
