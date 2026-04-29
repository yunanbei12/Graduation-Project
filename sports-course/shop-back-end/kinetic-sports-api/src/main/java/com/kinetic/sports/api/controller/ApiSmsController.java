package com.kinetic.sports.api.controller;

import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.SmsLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
public class ApiSmsController {

    private final SmsLogService smsLogService;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 发送验证码（注册时无需登录，绑定手机时需登录）
     * 参数: phone, type (0=注册验证 1=绑定手机验证)
     */
    @PostMapping("/send")
    public ServerResponseEntity<Void> sendCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String typeStr = params.getOrDefault("type", "0");
        int type;
        try {
            type = Integer.parseInt(typeStr);
        } catch (NumberFormatException e) {
            type = 0;
        }

        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            return ServerResponseEntity.fail("请输入正确的手机号");
        }

        try {
            smsLogService.sendCode(phone, type);
            return ServerResponseEntity.success();
        } catch (RuntimeException e) {
            return ServerResponseEntity.fail(e.getMessage());
        }
    }
}
