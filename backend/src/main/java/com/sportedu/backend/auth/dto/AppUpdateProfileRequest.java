package com.sportedu.backend.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AppUpdateProfileRequest(
    @Size(max = 50, message = "真实姓名长度不能超过50")
    String realName,
    @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号格式不正确")
    String phone,
    @Size(max = 50, message = "家长姓名长度不能超过50")
    String parentName,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate birthday,
    Integer gender
) {
}
