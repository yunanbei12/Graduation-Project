package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("`order`")
public class Order extends BaseEntity {

    private String orderNumber;

    private Long userId;

    /** 关联课程ID */
    private Long courseId;

    /** 课程版固定使用 1=课程订单 */
    private Integer orderType;

    private BigDecimal totalAmount;

    private BigDecimal actualAmount;

    /** 使用的优惠券ID */
    private Long couponId;

    /** 优惠券抵扣金额 */
    private BigDecimal couponAmount;

    /** 支付方式(wechat/alipay/cash) */
    private String paymentMethod;

    /** 支付时间 */
    private LocalDateTime paymentTime;

    /** 1=待支付 2=已支付 3=待处理 4=已完成 5=已取消 6=退款中 7=已退款 8=退款驳回 */
    private Integer status;

    private String remark;

    private Long scheduleId;

    private String refundReason;

    /** 退款申请前的原状态，用于驳回后恢复 */
    private Integer beforeRefundStatus;

    /** 本次退款金额 */
    private BigDecimal refundAmount;

    /** 订单关闭时间 */
    private LocalDateTime closeTime;

    /** 订单完成时间 */
    private LocalDateTime finishTime;

    /** 非数据库字段：团课批量下单时选中的排课ID列表 */
    @TableField(exist = false)
    private List<Long> scheduleIds;

    /** 非数据库字段：批量支付时选中的订单ID列表 */
    @TableField(exist = false)
    private List<Long> orderIds;

}
