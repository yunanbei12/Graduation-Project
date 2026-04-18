package com.kinetic.sports.api.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kinetic.sports.bean.model.Coupon;
import com.kinetic.sports.bean.model.User;
import com.kinetic.sports.bean.model.UserCoupon;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.CouponService;
import com.kinetic.sports.service.SmsLogService;
import com.kinetic.sports.service.UserCouponService;
import com.kinetic.sports.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class ApiAuthController {

    private final UserService userService;
    private final SmsLogService smsLogService;
    private final CouponService couponService;
    private final UserCouponService userCouponService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Value("${wx.mini.appid:}")
    private String wxAppid;

    @Value("${wx.mini.secret:}")
    private String wxSecret;

    /**
     * 微信登录（支持直接传openId或传code自动换取openId）
     */
    @PostMapping("/wxLogin")
    public ServerResponseEntity<Map<String, Object>> wxLogin(@RequestBody Map<String, String> params) {
        String openId = params.get("openId");
        String code = params.get("code");

        // 如果传的是 code，后端自动换取 openId
        if ((openId == null || openId.isEmpty()) && code != null && !code.isEmpty()) {
            openId = getOpenIdByCode(code);
            if (openId == null || openId.isEmpty()) {
                return ServerResponseEntity.fail("微信登录失败，请重试");
            }
        }

        if (openId == null || openId.isEmpty()) {
            return ServerResponseEntity.fail("openId不能为空");
        }
        User user = userService.getOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenId, openId)
        );
        if (user == null) {
            user = new User();
            user.setOpenId(openId);
            user.setNickName(params.getOrDefault("nickName", "微信用户"));
            user.setAvatarUrl(params.get("avatarUrl"));
            user.setRegisterType(0); // 微信注册
            user.setStatus(1);
            userService.save(user);
            // 发放注册赠送优惠券
            sendRegisterGiftCoupons(user.getId());
        }
        if (user.getStatus() != 1) {
            return ServerResponseEntity.fail("账号已被禁用");
        }
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("nickName", user.getNickName());
        result.put("avatarUrl", user.getAvatarUrl());
        result.put("phone", user.getPhone());
        result.put("hasPhone", user.getPhone() != null && !user.getPhone().isEmpty());
        return ServerResponseEntity.success(result);
    }

    /**
     * 手机号密码注册（需短信验证码）
     */
    @PostMapping("/register")
    public ServerResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String passWord = params.get("passWord");
        String code = params.get("code");

        // 1. 手机号格式校验
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            return ServerResponseEntity.fail("请输入正确的手机号");
        }
        // 2. 验证码校验
        if (code == null || code.trim().isEmpty()) {
            return ServerResponseEntity.fail("请输入验证码");
        }
        if (!smsLogService.verifyCode(phone, code.trim(), 0)) {
            return ServerResponseEntity.fail("验证码错误或已过期");
        }
        // 3. 手机号唯一性
        long count = userService.count(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone)
        );
        if (count > 0) {
            return ServerResponseEntity.fail("该手机号已注册");
        }
        // 4. 密码校验
        if (passWord == null || passWord.length() < 6) {
            return ServerResponseEntity.fail("密码不能少于6位");
        }

        // 5. 创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName("用户" + RandomUtil.randomNumbers(4));
        user.setLoginPassword(passwordEncoder.encode(passWord));
        user.setRegisterType(1); // 手机号注册
        user.setStatus(1);
        userService.save(user);
        // 发放注册赠送优惠券
        sendRegisterGiftCoupons(user.getId());

        return ServerResponseEntity.success(null);
    }

    /**
     * 手机号密码登录
     */
    @PostMapping("/login")
    public ServerResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String passWord = params.get("passWord");
        if (phone == null || phone.trim().isEmpty()) {
            return ServerResponseEntity.fail("请输入手机号");
        }
        User user = userService.getOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone)
        );
        if (user == null) {
            return ServerResponseEntity.fail("手机号或密码不正确");
        }
        // 针对验证码注册的用户（尚未设置密码）给出友好提示
        if (user.getLoginPassword() == null || user.getLoginPassword().isEmpty()) {
            return ServerResponseEntity.fail("您尚未设置登录密码，请使用验证码登录，或到『个人资料』中设置密码");
        }
        if (!passwordEncoder.matches(passWord, user.getLoginPassword())) {
            return ServerResponseEntity.fail("手机号或密码不正确");
        }
        if (user.getStatus() != 1) {
            return ServerResponseEntity.fail("账号已被禁用");
        }
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("nickName", user.getNickName());
        result.put("avatarUrl", user.getAvatarUrl());
        result.put("phone", user.getPhone());
        result.put("hasPhone", true);
        return ServerResponseEntity.success(result);
    }

    /**
     * 短信验证码登录
     * - 手机号已注册：验证码通过直接登录
     * - 手机号未注册：返回 notRegistered=true，由前端提示用户确认是否注册
     */
    @PostMapping("/smsLogin")
    public ServerResponseEntity<Map<String, Object>> smsLogin(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String code = params.get("code");
        String confirmRegister = params.get("confirmRegister"); // 前端确认注册后传 "true"

        // 1. 手机号格式校验
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            return ServerResponseEntity.fail("请输入正确的手机号");
        }
        // 2. 验证码校验（确认注册第二步跳过验证码校验，上一步已消耗）
        if (!"true".equals(confirmRegister)) {
            if (code == null || code.trim().isEmpty()) {
                return ServerResponseEntity.fail("请输入验证码");
            }
            if (!smsLogService.verifyCode(phone, code.trim(), 0)) {
                return ServerResponseEntity.fail("验证码错误或已过期");
            }
        }

        // 3. 查找用户
        User user = userService.getOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone)
        );

        if (user == null) {
            // 未注册：若前端未确认，则返回提示让用户确认
            if (!"true".equals(confirmRegister)) {
                Map<String, Object> result = new HashMap<>();
                result.put("notRegistered", true);
                result.put("phone", phone);
                return ServerResponseEntity.success(result);
            }
            // 前端已确认注册，自动创建账号
            user = new User();
            user.setPhone(phone);
            user.setNickName("用户" + RandomUtil.randomNumbers(4));
            user.setRegisterType(2); // 短信验证码登录注册
            user.setStatus(1);
            userService.save(user);
            // 发放注册赠送优惠券
            sendRegisterGiftCoupons(user.getId());
        }

        if (user.getStatus() != 1) {
            return ServerResponseEntity.fail("账号已被禁用");
        }

        // 4. 登录
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("nickName", user.getNickName());
        result.put("avatarUrl", user.getAvatarUrl());
        result.put("phone", user.getPhone());
        result.put("hasPhone", true);
        return ServerResponseEntity.success(result);
    }

    /**
     * 通过微信 code 换取 openId
     */
    private String getOpenIdByCode(String code) {
        if (wxAppid == null || wxAppid.isEmpty() || wxSecret == null || wxSecret.isEmpty()) {
            log.warn("微信小程序 appid/secret 未配置，无法换取 openId");
            return null;
        }
        try {
            String url = String.format(
                    "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    wxAppid, wxSecret, code
            );
            String response = HttpUtil.get(url);
            log.info("微信 jscode2session 响应: {}", response);
            // 解析 JSON 获取 openid
            cn.hutool.json.JSONObject json = new cn.hutool.json.JSONUtil().parseObj(response);
            if (json.containsKey("openid")) {
                return json.getStr("openid");
            }
            log.warn("微信登录失败: errcode={}, errmsg={}", json.get("errcode"), json.get("errmsg"));
            return null;
        } catch (Exception e) {
            log.error("微信 code 换取 openId 异常", e);
            return null;
        }
    }

    /**
     * 发放注册赠送优惠券
     */
    private void sendRegisterGiftCoupons(Long userId) {
        try {
            // 查找所有启用且设置为注册赠送的优惠券
            List<Coupon> giftCoupons = couponService.list(
                    new LambdaQueryWrapper<Coupon>()
                            .eq(Coupon::getStatus, 1)
                            .eq(Coupon::getRegisterGift, 1)
            );
            for (Coupon coupon : giftCoupons) {
                // 检查是否已发放
                long count = userCouponService.count(
                        new LambdaQueryWrapper<UserCoupon>()
                                .eq(UserCoupon::getUserId, userId)
                                .eq(UserCoupon::getCouponId, coupon.getId())
                );
                if (count > 0) continue;
                // 检查发放总量
                if (coupon.getTotalCount() > 0 && coupon.getUsedCount() >= coupon.getTotalCount()) {
                    continue;
                }
                // 发放
                UserCoupon userCoupon = new UserCoupon();
                userCoupon.setUserId(userId);
                userCoupon.setCouponId(coupon.getId());
                userCoupon.setStatus(0);
                userCouponService.save(userCoupon);
                // 更新发放数量
                coupon.setUsedCount(coupon.getUsedCount() + 1);
                couponService.updateById(coupon);
                log.info("注册赠送优惠券: userId={}, couponId={}, couponName={}", userId, coupon.getId(), coupon.getName());
            }
        } catch (Exception e) {
            log.error("发放注册赠送优惠券失败: userId={}", userId, e);
        }
    }
}
