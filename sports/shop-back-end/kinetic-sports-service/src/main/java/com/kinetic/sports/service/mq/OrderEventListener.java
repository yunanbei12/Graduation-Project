package com.kinetic.sports.service.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kinetic.sports.bean.dto.order.GroupScheduleCheckMessage;
import com.kinetic.sports.bean.dto.order.OrderDelayMessage;
import com.kinetic.sports.bean.dto.order.OrderLifecycleMessage;
import com.kinetic.sports.bean.model.Coupon;
import com.kinetic.sports.bean.model.Order;
import com.kinetic.sports.bean.model.UserCoupon;
import com.kinetic.sports.common.config.RabbitConfig;
import com.kinetic.sports.service.CouponService;
import com.kinetic.sports.service.OrderService;
import com.kinetic.sports.service.UserCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "biz.order", name = "rabbit-enabled", havingValue = "true")
public class OrderEventListener {

    private final OrderService orderService;
    private final CouponService couponService;
    private final UserCouponService userCouponService;

    @RabbitListener(queues = RabbitConfig.ORDER_CLOSE_PROCESS_QUEUE)
    public void handleOrderClose(OrderDelayMessage message) {
        if (message == null || message.getOrderId() == null) {
            return;
        }
        try {
            orderService.autoCancelOrder(message.getOrderId(), "超时未支付，系统自动取消");
        } catch (IllegalArgumentException e) {
            log.info("延迟关单跳过，orderId={}, reason={}", message.getOrderId(), e.getMessage());
        } catch (Exception e) {
            log.error("延迟关单处理失败，orderId={}", message.getOrderId(), e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitConfig.GROUP_CHECK_PROCESS_QUEUE)
    public void handleGroupCheck(GroupScheduleCheckMessage message) {
        if (message == null || message.getScheduleId() == null) {
            return;
        }
        try {
            orderService.checkGroupSchedule(message.getScheduleId());
        } catch (IllegalArgumentException e) {
            log.info("成团检查跳过，scheduleId={}, reason={}", message.getScheduleId(), e.getMessage());
        } catch (Exception e) {
            log.error("成团检查处理失败，scheduleId={}", message.getScheduleId(), e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitConfig.ORDER_PAID_QUEUE)
    public void handleOrderPaid(OrderLifecycleMessage message) {
        if (message == null || message.getOrderId() == null) {
            return;
        }
        log.info("订单支付成功事件已消费，orderId={}, orderNumber={}, orderType={}, status={}",
                message.getOrderId(), message.getOrderNumber(), message.getOrderType(), message.getStatus());

        // 活动发放：消费满额发放优惠券
        try {
            sendActivityCoupons(message.getOrderId());
        } catch (Exception e) {
            log.error("活动发放优惠券失败，orderId={}", message.getOrderId(), e);
        }
    }

    /**
     * 活动发放：消费满额发放优惠券
     */
    private void sendActivityCoupons(Long orderId) {
        // 获取订单信息
        Order order = orderService.getById(orderId);
        if (order == null || order.getUserId() == null) {
            return;
        }
        BigDecimal orderAmount = order.getActualAmount();
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // 查找所有启用且设置为活动发放的优惠券
        List<Coupon> activityCoupons = couponService.list(
                new LambdaQueryWrapper<Coupon>()
                        .eq(Coupon::getStatus, 1)
                        .eq(Coupon::getActivityTrigger, 1) // 消费满额触发
                        .isNotNull(Coupon::getActivityAmount)
        );

        for (Coupon coupon : activityCoupons) {
            // 检查是否满足触发金额
            if (orderAmount.compareTo(coupon.getActivityAmount()) < 0) {
                continue;
            }
            // 检查是否已发放
            long count = userCouponService.count(
                    new LambdaQueryWrapper<UserCoupon>()
                            .eq(UserCoupon::getUserId, order.getUserId())
                            .eq(UserCoupon::getCouponId, coupon.getId())
            );
            if (count > 0) {
                continue;
            }
            // 检查发放总量
            if (coupon.getTotalCount() > 0 && coupon.getUsedCount() >= coupon.getTotalCount()) {
                continue;
            }
            // 发放
            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setUserId(order.getUserId());
            userCoupon.setCouponId(coupon.getId());
            userCoupon.setStatus(0);
            userCouponService.save(userCoupon);
            // 更新发放数量
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponService.updateById(coupon);
            log.info("活动发放优惠券: userId={}, couponId={}, couponName={}, orderAmount={}, triggerAmount={}",
                    order.getUserId(), coupon.getId(), coupon.getName(), orderAmount, coupon.getActivityAmount());
        }
    }

    @RabbitListener(queues = RabbitConfig.ORDER_REFUNDED_QUEUE)
    public void handleOrderRefunded(OrderLifecycleMessage message) {
        if (message == null || message.getOrderId() == null) {
            return;
        }
        log.info("订单退款完成事件已消费，orderId={}, orderNumber={}, orderType={}, status={}",
                message.getOrderId(), message.getOrderNumber(), message.getOrderType(), message.getStatus());
    }
}
