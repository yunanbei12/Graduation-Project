package com.kinetic.sports.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.model.SmsLog;
import com.kinetic.sports.service.SmsLogService;
import com.kinetic.sports.service.mapper.SmsLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsLogServiceImpl extends ServiceImpl<SmsLogMapper, SmsLog> implements SmsLogService {

    /** 验证码有效期（分钟） */
    private static final int CODE_EXPIRE_MINUTES = 5;
    /** 同一手机号每天最多发送条数 */
    private static final int DAILY_MAX_SEND = 10;
    /** 同一手机号两次发送最小间隔（秒） */
    private static final int SEND_INTERVAL_SECONDS = 60;

    @Value("${sms.access-key-id:}")
    private String accessKeyId;

    @Value("${sms.access-key-secret:}")
    private String accessKeySecret;

    @Value("${sms.sign-name:}")
    private String signName;

    @Value("${sms.template-code:}")
    private String templateCode;

    @Override
    public void sendCode(String phone, int type) {
        // 1. 频率限制：同一手机号60秒内只能发一条
        SmsLog lastSms = getOne(
                new LambdaQueryWrapper<SmsLog>()
                        .eq(SmsLog::getUserPhone, phone)
                        .eq(SmsLog::getType, type)
                        .orderByDesc(SmsLog::getRecDate)
                        .last("LIMIT 1")
        );
        if (lastSms != null && lastSms.getRecDate() != null) {
            long secondsDiff = java.time.Duration.between(lastSms.getRecDate(), LocalDateTime.now()).getSeconds();
            if (secondsDiff < SEND_INTERVAL_SECONDS) {
                throw new RuntimeException("发送太频繁，请" + (SEND_INTERVAL_SECONDS - secondsDiff) + "秒后再试");
            }
        }

        // 2. 每日限制
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        long todayCount = count(
                new LambdaQueryWrapper<SmsLog>()
                        .eq(SmsLog::getUserPhone, phone)
                        .eq(SmsLog::getType, type)
                        .ge(SmsLog::getRecDate, todayStart)
        );
        if (todayCount >= DAILY_MAX_SEND) {
            throw new RuntimeException("今日验证码发送次数已达上限");
        }

        // 3. 将该手机号之前的验证码全部失效
        List<SmsLog> validSms = list(
                new LambdaQueryWrapper<SmsLog>()
                        .eq(SmsLog::getUserPhone, phone)
                        .eq(SmsLog::getType, type)
                        .eq(SmsLog::getStatus, 1)
        );
        validSms.forEach(s -> {
            s.setStatus(0);
            updateById(s);
        });

        // 4. 生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 5. 发送短信（开发模式：如果未配置阿里云SMS则只打日志）
        boolean smsSent = sendSmsViaAliyun(phone, code);

        // 6. 保存记录
        SmsLog smsLog = new SmsLog();
        smsLog.setUserPhone(phone);
        smsLog.setMobileCode(code);
        smsLog.setType(type);
        smsLog.setStatus(1);
        smsLog.setRecDate(LocalDateTime.now());
        if (smsSent) {
            smsLog.setContent("验证码已发送至" + phone);
        } else {
            smsLog.setContent("[开发模式]验证码: " + code);
        }
        save(smsLog);

        // 开发模式下日志输出验证码
        if (!smsSent) {
            log.info("========== [开发模式] 手机号: {} 验证码: {} ==========", phone, code);
        }
    }

    @Override
    public boolean verifyCode(String phone, String code, int type) {
        // 查询该手机号最新一条有效验证码
        SmsLog smsLog = getOne(
                new LambdaQueryWrapper<SmsLog>()
                        .eq(SmsLog::getUserPhone, phone)
                        .eq(SmsLog::getType, type)
                        .eq(SmsLog::getStatus, 1)
                        .orderByDesc(SmsLog::getRecDate)
                        .last("LIMIT 1")
        );
        if (smsLog == null) {
            return false;
        }
        // 检查是否过期（5分钟内有效）
        if (smsLog.getRecDate().plusMinutes(CODE_EXPIRE_MINUTES).isBefore(LocalDateTime.now())) {
            smsLog.setStatus(0);
            updateById(smsLog);
            return false;
        }
        // 校验验证码
        if (smsLog.getMobileCode().equals(code)) {
            // 验证成功，失效该验证码
            smsLog.setStatus(0);
            updateById(smsLog);
            return true;
        }
        return false;
    }

    /**
     * 通过阿里云短信服务发送验证码
     * @return true=发送成功 false=未配置或发送失败（开发模式回退）
     */
    private boolean sendSmsViaAliyun(String phone, String code) {
        if (accessKeyId == null || accessKeyId.isEmpty() ||
            accessKeySecret == null || accessKeySecret.isEmpty()) {
            // 未配置阿里云SMS，回退到开发模式
            return false;
        }
        try {
            com.aliyuncs.profile.DefaultProfile profile =
                    com.aliyuncs.profile.DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
            com.aliyuncs.IAcsClient acsClient = new com.aliyuncs.DefaultAcsClient(profile);
            com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest request =
                    new com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest();
            request.setPhoneNumbers(phone);
            request.setSignName(signName);
            request.setTemplateCode(templateCode);
            request.setTemplateParam("{\"code\":\"" + code + "\"}");
            com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse response = acsClient.getAcsResponse(request);
            if (response.getCode() != null && "OK".equals(response.getCode())) {
                return true;
            } else {
                log.warn("阿里云短信发送失败: code={}, message={}", response.getCode(), response.getMessage());
                return false;
            }
        } catch (Exception e) {
            log.warn("阿里云短信发送异常: {}", e.getMessage());
            return false;
        }
    }
}
