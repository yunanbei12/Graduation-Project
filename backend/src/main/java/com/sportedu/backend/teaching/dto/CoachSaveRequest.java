package com.sportedu.backend.teaching.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CoachSaveRequest(
    @NotBlank(message = "教练姓名不能为空")
    @Size(max = 50, message = "教练姓名长度不能超过50")
    String coachName,
    @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号格式不正确")
    String phone,
    @Size(max = 64, message = "身份证长度不能超过64")
    String idCardNo,
    Integer gender,
    List<String> sportItems,
    String introduction,
    @DecimalMin(value = "0.00", message = "课时费不能小于0")
    BigDecimal hourlyRate,
    List<String> availableTimes,
    Integer status
) {
}
