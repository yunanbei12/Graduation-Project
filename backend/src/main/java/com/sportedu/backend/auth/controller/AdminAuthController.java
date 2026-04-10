package com.sportedu.backend.auth.controller;

import com.sportedu.backend.auth.dto.AdminLoginRequest;
import com.sportedu.backend.auth.dto.AdminLoginResponse;
import com.sportedu.backend.auth.dto.AdminMeResponse;
import com.sportedu.backend.auth.service.AdminAuthService;
import com.sportedu.backend.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.success(adminAuthService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<AdminMeResponse> me() {
        return ApiResponse.success(adminAuthService.currentAdmin());
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success();
    }
}
