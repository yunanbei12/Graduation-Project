package com.kinetic.sports.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.bean.model.SysUser;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;

    @PostMapping("/login")
    public ServerResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        SysUser sysUser = sysUserService.getOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
        if (sysUser == null) {
            return ServerResponseEntity.fail("用户名或密码错误");
        }

        // 密码校验：支持MD5和BCrypt两种方式
        String inputPassword = md5(password);
        if (!sysUser.getPassword().equals(inputPassword) && !sysUser.getPassword().equals(password) && !matchesBcrypt(password, sysUser.getPassword())) {
            return ServerResponseEntity.fail("用户名或密码错误");
        }

        if (sysUser.getStatus() != 1) {
            return ServerResponseEntity.fail("账号已被禁用");
        }

        StpUtil.login(sysUser.getId());
        String token = StpUtil.getTokenValue();

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", sysUser.getId());
        result.put("nickName", sysUser.getNickName());
        return ServerResponseEntity.success(result);
    }

    @GetMapping("/info")
    public ServerResponseEntity<SysUser> info() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser sysUser = sysUserService.getById(userId);
        sysUser.setPassword(null);
        return ServerResponseEntity.success(sysUser);
    }

    @PostMapping("/logout")
    public ServerResponseEntity<Void> logout() {
        StpUtil.logout();
        return ServerResponseEntity.success();
    }

    @PutMapping("/password")
    public ServerResponseEntity<Void> updatePassword(@RequestBody Map<String, String> params) {
        Long userId = StpUtil.getLoginIdAsLong();
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        SysUser sysUser = sysUserService.getById(userId);
        if (!sysUser.getPassword().equals(oldPassword)) {
            return ServerResponseEntity.fail("原密码错误");
        }
        sysUser.setPassword(newPassword);
        sysUserService.updateById(sysUser);
        return ServerResponseEntity.success();
    }

    private boolean matchesBcrypt(String rawPassword, String encodedPassword) {
        try {
            return new BCryptPasswordEncoder().matches(rawPassword, encodedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return input;
        }
    }
}
