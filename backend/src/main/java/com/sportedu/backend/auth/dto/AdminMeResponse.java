package com.sportedu.backend.auth.dto;

import java.util.List;

public record AdminMeResponse(
    Long adminId,
    String username,
    String realName,
    String phone,
    String email,
    Integer status,
    List<String> roles
) {
}
