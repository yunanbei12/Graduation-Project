package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    private String nickName;

    private String avatarUrl;

    private String phone;

    private String openId;

    /** 账号密码登录密码（BCrypt加密） */
    private String loginPassword;

    /** 注册方式 0=微信注册 1=手机号注册 2=短信验证码登录 */
    private Integer registerType;

    private Integer status;
}
