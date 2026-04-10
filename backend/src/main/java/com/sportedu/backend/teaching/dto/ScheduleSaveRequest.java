package com.sportedu.backend.teaching.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ScheduleSaveRequest(
    @NotNull(message = "课程ID不能为空")
    Long courseId,
    @NotNull(message = "教练ID不能为空")
    Long coachId,
    @NotNull(message = "排课日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate scheduleDate,
    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime startTime,
    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime endTime,
    @NotBlank(message = "上课地点不能为空")
    String location,
    @NotNull(message = "容量不能为空")
    Integer capacity,
    @NotNull(message = "最小成团人数不能为空")
    Integer minGroupCount
) {
}
