package com.sportedu.backend.auth.dto;

import java.time.LocalDate;

public record AppMeResponse(
    Long userId,
    String nickname,
    String avatarUrl,
    String phone,
    String parentName,
    Integer status,
    String realName,
    LocalDate birthday,
    Integer gender
) {
}
