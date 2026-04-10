package com.sportedu.backend.auth;

import com.sportedu.backend.common.enums.ResultCode;
import com.sportedu.backend.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser currentUser)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return currentUser;
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }
}
