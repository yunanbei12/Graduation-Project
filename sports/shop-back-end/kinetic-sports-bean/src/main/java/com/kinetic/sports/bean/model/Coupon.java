package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coupon")
public class Coupon extends BaseEntity {

    private String name;

    /** 1=满减 2=折扣 3=无门槛 */
    private Integer type;

    /** 满减金额/无门槛金额 */
    private BigDecimal discount;

    /** 使用门槛金额 */
    private BigDecimal minAmount;

    /** 折扣率(0.8=8折) */
    private BigDecimal discountRatio;

    /** 1=全场 2=仅课程 3=仅商品 */
    private Integer scope;

    /** 发放总量(0=不限) */
    private Integer totalCount;

    /** 已使用数量 */
    private Integer usedCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 0=禁用 1=启用 */
    private Integer status;

    /** 是否注册赠送 0=否 1=是 */
    private Integer registerGift;

    /** 活动触发类型 null=无 1=消费满额 */
    private Integer activityTrigger;

    /** 活动触发金额 */
    private BigDecimal activityAmount;
}
