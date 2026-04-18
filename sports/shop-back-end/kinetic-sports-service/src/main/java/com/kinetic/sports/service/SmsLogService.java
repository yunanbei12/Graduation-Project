package com.kinetic.sports.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kinetic.sports.bean.model.SmsLog;

public interface SmsLogService extends IService<SmsLog> {

    /**
     * 发送验证码
     * @param phone 手机号
     * @param type 类型 0=注册验证 1=绑定手机验证
     */
    void sendCode(String phone, int type);

    /**
     * 校验验证码
     * @param phone 手机号
     * @param code 验证码
     * @param type 类型
     * @return 是否校验通过
     */
    boolean verifyCode(String phone, String code, int type);
}
