package com.sportedu.backend.auth;

import java.util.List;

public class CurrentUser {

    private final Long userId;
    private final String subject;
    private final String roleType;
    private final List<String> roles;

    public CurrentUser(Long userId, String subject, String roleType, List<String> roles) {
        this.userId = userId;
        this.subject = subject;
        this.roleType = roleType;
        this.roles = roles;
    }

    public Long getUserId() {
        return userId;
    }

    public String getSubject() {
        return subject;
    }

    public String getRoleType() {
        return roleType;
    }

    public List<String> getRoles() {
        return roles;
    }
}
