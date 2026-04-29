package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sms_log")
public class SmsLog extends BaseEntity {

    private String userPhone;

    private String mobileCode;

    /** 0=注册验证 1=绑定手机验证 */
    private Integer type;

    private String content;

    /** 1=有效 0=已失效 */
    private Integer status;

    private LocalDateTime recDate;
}
