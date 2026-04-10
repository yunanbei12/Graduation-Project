package com.sportedu.backend.teaching.dto;

import jakarta.validation.constraints.Size;

public record CancelScheduleRequest(
    @Size(max = 255, message = "取消原因长度不能超过255")
    String reason
) {
}
