package com.sportedu.backend.auth.controller;

import com.sportedu.backend.auth.dto.AppLoginResponse;
import com.sportedu.backend.auth.dto.AppMeResponse;
import com.sportedu.backend.auth.dto.AppUpdateProfileRequest;
import com.sportedu.backend.auth.dto.AppWxLoginRequest;
import com.sportedu.backend.common.api.ApiResponse;
import com.sportedu.backend.user.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/v1/auth")
public class AppAuthController {

    private final AppUserService appUserService;

    public AppAuthController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @PostMapping("/wx-login")
    public ApiResponse<AppLoginResponse> wxLogin(@Valid @RequestBody AppWxLoginRequest request) {
        return ApiResponse.success(appUserService.wxLogin(request));
    }

    @GetMapping("/me")
    public ApiResponse<AppMeResponse> me() {
        return ApiResponse.success(appUserService.currentUser());
    }

    @PutMapping("/profile")
    public ApiResponse<AppMeResponse> updateProfile(@Valid @RequestBody AppUpdateProfileRequest request) {
        return ApiResponse.success(appUserService.updateProfile(request));
    }
}
