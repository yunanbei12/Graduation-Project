package com.sportedu.backend.course.dto;

import jakarta.validation.constraints.NotNull;

public record CourseStatusRequest(
    @NotNull(message = "状态不能为空")
    Integer status
) {
}
