package com.sportedu.backend.course.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CourseSaveRequest(
    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100, message = "课程名称长度不能超过100")
    String courseName,
    @Size(max = 50, message = "课程编码长度不能超过50")
    String courseCode,
    @NotNull(message = "课程类型不能为空")
    Integer courseType,
    @NotBlank(message = "运动类型不能为空")
    @Size(max = 50, message = "运动类型长度不能超过50")
    String sportType,
    @Size(max = 255, message = "封面地址长度不能超过255")
    String coverUrl,
    String description,
    List<String> detailImages,
    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.00", message = "售价不能小于0")
    BigDecimal price,
    @DecimalMin(value = "0.00", message = "原价不能小于0")
    BigDecimal originalPrice,
    Integer lessonCount,
    Integer validityDays,
    Integer isDoorToDoor,
    List<String> serviceAreas,
    @Size(max = 100, message = "固定排课说明长度不能超过100")
    String fixedScheduleDesc,
    @Size(max = 100, message = "固定地点长度不能超过100")
    String fixedLocation,
    Integer maxParticipants,
    Integer groupSuccessCount,
    Integer operationWeight,
    Integer status
) {
}
