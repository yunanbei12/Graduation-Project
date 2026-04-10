package com.sportedu.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AppWxLoginRequest(
    @NotBlank(message = "微信 code 不能为空")
    String code,
    @Size(max = 100, message = "昵称长度不能超过100")
    String nickname,
    @Size(max = 255, message = "头像地址长度不能超过255")
    String avatarUrl
) {
}
