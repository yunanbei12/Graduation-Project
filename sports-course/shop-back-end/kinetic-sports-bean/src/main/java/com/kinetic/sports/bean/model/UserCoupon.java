package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_coupon")
public class UserCoupon extends BaseEntity {

    private Long userId;

    private Long couponId;

    private Long orderId;

    /** 0=未使用 1=已使用 2=已过期 3=已锁定 */
    private Integer status;

    private LocalDateTime useTime;
}
