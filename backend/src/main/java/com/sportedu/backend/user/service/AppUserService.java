package com.sportedu.backend.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportedu.backend.auth.JwtTokenService;
import com.sportedu.backend.auth.SecurityUtils;
import com.sportedu.backend.auth.dto.AppLoginResponse;
import com.sportedu.backend.auth.dto.AppMeResponse;
import com.sportedu.backend.auth.dto.AppUpdateProfileRequest;
import com.sportedu.backend.auth.dto.AppWxLoginRequest;
import com.sportedu.backend.common.enums.ResultCode;
import com.sportedu.backend.common.exception.BusinessException;
import com.sportedu.backend.user.entity.User;
import com.sportedu.backend.user.entity.UserProfile;
import com.sportedu.backend.user.mapper.UserMapper;
import com.sportedu.backend.user.mapper.UserProfileMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppUserService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final JwtTokenService jwtTokenService;

    public AppUserService(UserMapper userMapper,
                          UserProfileMapper userProfileMapper,
                          JwtTokenService jwtTokenService) {
        this.userMapper = userMapper;
        this.userProfileMapper = userProfileMapper;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AppLoginResponse wxLogin(AppWxLoginRequest request) {
        String openid = "mock_" + request.code().trim();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
            .eq(User::getOpenid, openid)
            .eq(User::getDeleted, 0)
            .last("limit 1"));

        boolean isNewUser = false;
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname(StringUtils.hasText(request.nickname()) ? request.nickname() : "微信用户");
            user.setAvatarUrl(request.avatarUrl());
            user.setRegisterSource(1);
            user.setStatus(1);
            user.setDeleted(0);
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.insert(user);
            createDefaultProfile(user.getId());
            isNewUser = true;
        } else {
            if (StringUtils.hasText(request.nickname())) {
                user.setNickname(request.nickname());
            }
            if (StringUtils.hasText(request.avatarUrl())) {
                user.setAvatarUrl(request.avatarUrl());
            }
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);
        }

        String token = jwtTokenService.generateToken(user.getId(), user.getOpenid(), "APP", List.of("APP_USER"));
        return new AppLoginResponse(token, user.getId(), user.getOpenid(), user.getNickname(), isNewUser);
    }

    public AppMeResponse currentUser() {
        User user = getCurrentEnabledUser();
        return toMeResponse(user);
    }

    @Transactional
    public AppMeResponse updateProfile(AppUpdateProfileRequest request) {
        User user = getCurrentEnabledUser();
        user.setRealName(request.realName());
        user.setPhone(request.phone());
        user.setParentName(request.parentName());
        user.setBirthday(request.birthday());
        user.setGender(request.gender());
        userMapper.updateById(user);
        return toMeResponse(userMapper.selectById(user.getId()));
    }

    private User getCurrentEnabledUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null || !Integer.valueOf(0).equals(user.getDeleted())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已禁用");
        }
        return user;
    }

    private AppMeResponse toMeResponse(User user) {
        return new AppMeResponse(
            user.getId(),
            user.getNickname(),
            user.getAvatarUrl(),
            user.getPhone(),
            user.getParentName(),
            user.getStatus(),
            user.getRealName(),
            user.getBirthday(),
            user.getGender()
        );
    }

    private void createDefaultProfile(Long userId) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setPurchaseCount(0);
        profile.setProfileSource(1);
        profile.setDeleted(0);
        userProfileMapper.insert(profile);
    }
}
