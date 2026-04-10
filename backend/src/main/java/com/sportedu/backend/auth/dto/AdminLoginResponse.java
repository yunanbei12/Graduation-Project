package com.sportedu.backend.auth.dto;

import java.util.List;

public record AdminLoginResponse(
    String token,
    Long adminId,
    String username,
    String realName,
    List<String> roles
) {
}
