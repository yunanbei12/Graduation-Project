package com.kinetic.sports.api.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kinetic.sports.bean.model.User;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.SmsLogService;
import com.kinetic.sports.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ApiProfileController {

    private final UserService userService;
    private final SmsLogService smsLogService;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 获取当前用户信息（附带 hasPassword 字段）
     */
    @GetMapping("/info")
    public ServerResponseEntity<Map<String, Object>> info() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        if (user == null) {
            return ServerResponseEntity.fail("用户不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("nickName", user.getNickName());
        result.put("avatarUrl", user.getAvatarUrl());
        result.put("phone", user.getPhone());
        result.put("status", user.getStatus());
        result.put("createTime", user.getCreateTime());
        result.put("hasPhone", user.getPhone() != null && !user.getPhone().isEmpty());
        result.put("hasPassword", user.getLoginPassword() != null && !user.getLoginPassword().isEmpty());
        return ServerResponseEntity.success(result);
    }

    /**
     * 更新用户信息（只允许修改昵称、头像，手机号不可改）
     */
    @PutMapping("/update")
    public ServerResponseEntity<Void> update(@RequestBody User user) {
        Long userId = StpUtil.getLoginIdAsLong();
        User dbUser = userService.getById(userId);
        if (dbUser == null) {
            return ServerResponseEntity.fail("用户不存在");
        }
        // 只允许修改昵称、头像（手机号不可修改，需通过 /profile/bind-phone 接口）
        if (user.getNickName() != null) dbUser.setNickName(user.getNickName());
        if (user.getAvatarUrl() != null) dbUser.setAvatarUrl(user.getAvatarUrl());
        userService.updateById(dbUser);
        return ServerResponseEntity.success();
    }

    /**
     * 绑定手机号（微信用户首次绑定）
     * 参数: phone, code
     */
    @PostMapping("/bind-phone")
    public ServerResponseEntity<Map<String, Object>> bindPhone(@RequestBody Map<String, String> params) {
        Long userId = StpUtil.getLoginIdAsLong();
        String phone = params.get("phone");
        String code = params.get("code");

        // 1. 参数校验
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            return ServerResponseEntity.fail("请输入正确的手机号");
        }
        if (code == null || code.trim().isEmpty()) {
            return ServerResponseEntity.fail("请输入验证码");
        }

        // 2. 验证码校验
        if (!smsLogService.verifyCode(phone, code.trim(), 1)) {
            return ServerResponseEntity.fail("验证码错误或已过期");
        }

        // 3. 手机号唯一性校验
        long count = userService.count(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone)
        );
        if (count > 0) {
            return ServerResponseEntity.fail("该手机号已被其他账号绑定");
        }

        // 4. 更新用户手机号
        User user = userService.getById(userId);
        if (user == null) {
            return ServerResponseEntity.fail("用户不存在");
        }
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            return ServerResponseEntity.fail("您已绑定手机号，不可修改");
        }
        user.setPhone(phone);
        userService.updateById(user);

        // 5. 返回更新后的用户信息
        Map<String, Object> result = new HashMap<>();
        result.put("phone", user.getPhone());
        result.put("hasPhone", true);
        return ServerResponseEntity.success(result);
    }

    /**
     * 修改密码
     * 请求体: { oldPassword, newPassword }
     * - 微信注册用户若从未设置密码，oldPassword 传空串即可直接设置
     */
    @PutMapping("/password")
    public ServerResponseEntity<Void> updatePassword(@RequestBody Map<String, String> params) {
        Long userId = StpUtil.getLoginIdAsLong();
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            return ServerResponseEntity.fail("新密码不能少于6位");
        }
        User dbUser = userService.getById(userId);
        if (dbUser == null) {
            return ServerResponseEntity.fail("用户不存在");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // 若已有密码，需验证旧密码
        if (dbUser.getLoginPassword() != null && !dbUser.getLoginPassword().isEmpty()) {
            if (oldPassword == null || !encoder.matches(oldPassword, dbUser.getLoginPassword())) {
                return ServerResponseEntity.fail("原密码不正确");
            }
        }
        dbUser.setLoginPassword(encoder.encode(newPassword));
        userService.updateById(dbUser);
        return ServerResponseEntity.success();
    }
}
