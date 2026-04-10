package com.sportedu.backend.auth.dto;

public record AppLoginResponse(
    String token,
    Long userId,
    String openid,
    String nickname,
    boolean isNewUser
) {
}
