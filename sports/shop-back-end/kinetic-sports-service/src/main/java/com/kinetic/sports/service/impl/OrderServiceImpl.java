package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.dto.order.OrderLifecycleMessage;
import com.kinetic.sports.common.config.OrderBizProperties;
import com.kinetic.sports.bean.model.*;
import com.kinetic.sports.service.*;
import com.kinetic.sports.service.mq.OrderEventPublisher;
import com.kinetic.sports.service.mq.TxEventHelper;
import com.kinetic.sports.service.mapper.OrderMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private static final int USER_COUPON_UNUSED = 0;
    private static final int USER_COUPON_USED = 1;
    private static final int USER_COUPON_LOCKED = 3;

    private static final int ORDER_PENDING = 1;
    private static final int ORDER_PAID = 2;
    private static final int ORDER_PROCESSING = 3;
    private static final int ORDER_FINISHED = 4;
    private static final int ORDER_CANCELED = 5;
    private static final int ORDER_REFUNDING = 6;
    private static final int ORDER_REFUNDED = 7;

    private final OrderItemService orderItemService;
    private final CartService cartService;
    private final ProdService prodService;
    private final SkuService skuService;
    private final CourseService courseService;
    private final CourseScheduleService courseScheduleService;
    private final UserCoursePackageService userCoursePackageService;
    private final UserCouponService userCouponService;
    private final CouponService couponService;
    private final UserService userService;
    private final RedissonClient redissonClient;
    private final OrderBizProperties orderBizProperties;
    private final OrderEventPublisher orderEventPublisher;
    private final TxEventHelper txEventHelper;
    private final UserAddressService userAddressService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createCourseOrder(Long userId, Order params) {
        User currentUser = requireUser(userId);
        ensurePhoneBound(currentUser);

        Course course = courseService.getById(params.getCourseId());
        if (course == null || course.getStatus() != 1) {
            throw new IllegalArgumentException("课程不存在或已下架");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setCourseId(course.getId());
        order.setOrderType(1);
        order.setStatus(ORDER_PENDING);
        order.setOrderNumber(generateOrderNumber());
        order.setRemark(params.getRemark());

        if (course.getType() == 2) {
            if (params.getScheduleId() == null) {
                throw new IllegalArgumentException("请选择上课时间");
            }
            CourseSchedule schedule = courseScheduleService.getById(params.getScheduleId());
            if (schedule == null || schedule.getStatus() != 0) {
                throw new IllegalArgumentException("所选排课不存在或已无法报名");
            }
            if (schedule.getStartTime() != null && !schedule.getStartTime().isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("该场次已开始，无法报名");
            }
            // 新逻辑：使用scheduleDate+课程时间段判断
            LocalDateTime scheduleStart = resolveStartTime(schedule, course);
            if (scheduleStart != null && !scheduleStart.isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("该场次已开始，无法报名");
            }
            if (schedule.getEnrolledSeats() >= schedule.getTotalSeats()) {
                throw new IllegalArgumentException("该排课名额已满");
            }
            long existCount = this.count(new LambdaQueryWrapper<Order>()
                    .eq(Order::getUserId, userId)
                    .eq(Order::getScheduleId, params.getScheduleId())
                    .in(Order::getStatus, ORDER_PENDING, ORDER_PAID, ORDER_PROCESSING, ORDER_FINISHED, ORDER_REFUNDING));
            if (existCount > 0) {
                throw new IllegalArgumentException("您已报名该场次，请勿重复购买");
            }
            order.setScheduleId(params.getScheduleId());
        }

        BigDecimal totalAmount = course.getPrice();
        CouponPricing pricing = calculateCouponPricing(userId, params.getCouponId(), totalAmount, 1);
        order.setCouponId(pricing.userCouponId());
        order.setTotalAmount(totalAmount);
        order.setCouponAmount(pricing.couponAmount());
        order.setActualAmount(totalAmount.subtract(pricing.couponAmount()));
        this.save(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setItemType(1);
        item.setItemId(course.getId());
        item.setItemName(course.getName());
        item.setItemPic(course.getPic());
        item.setPrice(course.getPrice());
        item.setQty(1);
        orderItemService.save(item);

        lockCouponForOrder(pricing.userCouponId(), userId, order.getId());
        txEventHelper.afterCommit(() -> orderEventPublisher.publishOrderCloseDelay(order.getId()));
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createProdOrder(Long userId, Order params) {
        User currentUser = requireUser(userId);
        ensurePhoneBound(currentUser);

        List<ProductLine> lines = buildProductLines(userId, params);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("购物车为空");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ProductLine line : lines) {
            totalAmount = totalAmount.add(line.price.multiply(BigDecimal.valueOf(line.qty)));
        }

        CouponPricing pricing = calculateCouponPricing(userId, params.getCouponId(), totalAmount, 2);

        Order order = new Order();
        order.setUserId(userId);
        order.setOrderType(2);
        order.setStatus(ORDER_PENDING);
        order.setOrderNumber(generateOrderNumber());
        order.setRemark(params.getRemark());
        order.setCouponId(pricing.userCouponId());
        order.setTotalAmount(totalAmount);
        order.setCouponAmount(pricing.couponAmount());
        order.setActualAmount(totalAmount.subtract(pricing.couponAmount()));
        
        // 保存地址快照
        if (params.getAddressId() != null) {
            UserAddress address = userAddressService.getById(params.getAddressId());
            if (address != null && address.getUserId().equals(userId)) {
                try {
                    order.setAddressSnapshot(objectMapper.writeValueAsString(address));
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        
        this.save(order);

        for (ProductLine line : lines) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setItemType(2);
            item.setItemId(line.prod.getId());
            item.setItemName(line.prod.getName());
            item.setItemPic(line.prod.getPic());
            item.setPrice(line.price);
            item.setQty(line.qty);
            item.setSkuId(line.sku == null ? null : line.sku.getId());
            item.setSkuProperties(line.sku == null ? null : line.sku.getProperties());
            orderItemService.save(item);
        }

        lockCouponForOrder(pricing.userCouponId(), userId, order.getId());
        txEventHelper.afterCommit(() -> orderEventPublisher.publishOrderCloseDelay(order.getId()));

        if (params.getCartIds() != null && !params.getCartIds().isEmpty()) {
            cartService.removeByIds(params.getCartIds());
        } else if (params.getProdId() == null) {
            cartService.remove(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId) {
        Order order = requireOwnedOrder(userId, orderId);
        cancelPendingOrder(order, "用户取消订单");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoCancelOrder(Long orderId, String reason) {
        Order order = requireOrder(orderId);
        if (order.getStatus() != ORDER_PENDING) {
            throw new IllegalArgumentException("订单已支付或已关闭，无需自动取消");
        }
        cancelPendingOrder(order, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long userId, Long orderId) {
        Order order = requireOwnedOrder(userId, orderId);
        doPay(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrderByAdmin(Long orderId) {
        Order order = requireOrder(orderId);
        doPay(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRefund(Long userId, Long orderId, String refundReason) {
        Order order = requireOwnedOrder(userId, orderId);
        boolean prodAfterSaleWindow = order.getOrderType() == 2
                && order.getStatus() == ORDER_FINISHED
                && order.getFinishTime() != null
                && Duration.between(order.getFinishTime(), LocalDateTime.now()).toDays() <= 7;
        if (order.getStatus() != ORDER_PAID && order.getStatus() != ORDER_PROCESSING && !prodAfterSaleWindow) {
            throw new IllegalArgumentException("当前订单状态无法退款");
        }

        if (order.getOrderType() == 1) {
            Course course = courseService.getById(order.getCourseId());
            if (course != null && course.getType() == 2 && order.getScheduleId() != null) {
                CourseSchedule schedule = courseScheduleService.getById(order.getScheduleId());
                if (schedule != null) {
                    Course course2 = courseService.getById(order.getCourseId());
                    LocalDateTime scheduleStart = schedule.getStartTime() != null
                            ? schedule.getStartTime() : resolveStartTime(schedule, course2);
                    if (scheduleStart != null) {
                        long minutes = Duration.between(LocalDateTime.now(), scheduleStart).toMinutes();
                        if (minutes <= 120) {
                            throw new IllegalArgumentException("开课前 2 小时内不可退款");
                        }
                        if (minutes <= 480) {
                            order.setRemark(appendRemark(order.getRemark(), "开课前 2-8 小时内退款，默认扣除 20% 违约金"));
                        }
                    }
                }
            }
        }

        order.setBeforeRefundStatus(order.getStatus());
        order.setStatus(ORDER_REFUNDING);
        order.setRefundReason(refundReason);
        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRefund(Long orderId) {
        Order order = requireOrder(orderId);
        if (order.getStatus() != ORDER_REFUNDING) {
            throw new IllegalArgumentException("当前订单不在退款审核中");
        }
        finalizeRefund(order, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRefund(Long orderId) {
        Order order = requireOrder(orderId);
        if (order.getStatus() != ORDER_REFUNDING) {
            throw new IllegalArgumentException("当前订单不在退款审核中");
        }
        int fallbackStatus = order.getOrderType() == 1 ? ORDER_PROCESSING : ORDER_PAID;
        order.setStatus(order.getBeforeRefundStatus() == null ? fallbackStatus : order.getBeforeRefundStatus());
        order.setBeforeRefundStatus(null);
        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoRefund(Long orderId, String reason) {
        Order order = requireOrder(orderId);
        if (order.getStatus() != ORDER_PAID && order.getStatus() != ORDER_PROCESSING && order.getStatus() != ORDER_REFUNDING) {
            throw new IllegalArgumentException("当前订单状态无法自动退款");
        }
        if (!StringUtils.hasText(order.getRefundReason())) {
            order.setRefundReason(reason);
        }
        if (order.getStatus() != ORDER_REFUNDING) {
            order.setBeforeRefundStatus(order.getStatus());
            order.setStatus(ORDER_REFUNDING);
            this.updateById(order);
        }
        finalizeRefund(order, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipProdOrder(Long orderId) {
        Order order = requireOrder(orderId);
        if (order.getOrderType() != 2 || order.getStatus() != ORDER_PAID) {
            throw new IllegalArgumentException("当前订单无法发货");
        }
        order.setStatus(ORDER_PROCESSING);
        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(Long userId, Long orderId) {
        Order order = requireOrder(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作该订单");
        }
        if (order.getOrderType() != 2 || order.getStatus() != ORDER_PROCESSING) {
            throw new IllegalArgumentException("当前订单无法确认收货");
        }
        order.setStatus(ORDER_FINISHED);
        order.setFinishTime(LocalDateTime.now());
        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishOrder(Long orderId) {
        Order order = requireOrder(orderId);
        if (order.getStatus() != ORDER_PROCESSING) {
            throw new IllegalArgumentException("当前订单无法完成");
        }
        order.setStatus(ORDER_FINISHED);
        order.setFinishTime(LocalDateTime.now());
        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> checkGroupSchedule(Long scheduleId) {
        CourseSchedule schedule = courseScheduleService.getById(scheduleId);
        if (schedule == null) {
            throw new IllegalArgumentException("排课不存在");
        }

        RLock scheduleLock = redissonClient.getLock("order:schedule:check:" + scheduleId);
        scheduleLock.lock();
        try {
            schedule = courseScheduleService.getById(scheduleId);
            if (schedule == null) {
                throw new IllegalArgumentException("排课不存在");
            }

            Course course = courseService.getById(schedule.getCourseId());
            int minGroupSize = (course != null && course.getMinGroupSize() != null) ? course.getMinGroupSize() : 1;
            long enrolledCount = this.count(new LambdaQueryWrapper<Order>()
                    .eq(Order::getScheduleId, scheduleId)
                    .in(Order::getStatus, ORDER_PAID, ORDER_PROCESSING, ORDER_FINISHED));

            Map<String, Object> result = new HashMap<>();
            result.put("scheduleId", scheduleId);
            result.put("enrolledCount", enrolledCount);
            result.put("minGroupSize", minGroupSize);

            if (schedule.getStatus() == 3) {
                result.put("grouped", false);
                result.put("message", "排课已取消");
                return result;
            }

            if (enrolledCount >= minGroupSize) {
                if (schedule.getStatus() == 0) {
                    schedule.setStatus(1);
                    courseScheduleService.updateById(schedule);
                }
                result.put("grouped", true);
                result.put("message", "已成团，共 " + enrolledCount + " 人报名");
                return result;
            }

            if (schedule.getStatus() == 0) {
                schedule.setStatus(3);
                courseScheduleService.updateById(schedule);
            }

            List<Order> pendingOrders = this.list(new LambdaQueryWrapper<Order>()
                    .eq(Order::getScheduleId, scheduleId)
                    .eq(Order::getStatus, ORDER_PENDING));
            for (Order order : pendingOrders) {
                autoCancelOrder(order.getId(), "未达到成团人数，已自动取消");
            }

            List<Order> paidOrders = this.list(new LambdaQueryWrapper<Order>()
                    .eq(Order::getScheduleId, scheduleId)
                    .in(Order::getStatus, ORDER_PAID, ORDER_PROCESSING));
            for (Order order : paidOrders) {
                autoRefund(order.getId(), "未达到成团人数，自动退款");
            }

            result.put("grouped", false);
            result.put("message", "未成团（当前 " + enrolledCount + " 人，需 " + minGroupSize + " 人），排课已取消");
            return result;
        } finally {
            scheduleLock.unlock();
        }
    }

    @Override
    public Map<String, Object> previewRefund(Long userId, Long orderId) {
        Order order = requireOwnedOrder(userId, orderId);
        Map<String, Object> preview = new HashMap<>();
        preview.put("orderId", order.getId());
        preview.put("orderType", order.getOrderType());
        preview.put("status", order.getStatus());
        preview.put("actualAmount", order.getActualAmount());
        preview.put("estimatedRefundAmount", order.getActualAmount());
        preview.put("ruleType", "default");
        preview.put("ruleTip", "当前订单支持按实付金额原路退款。");
        preview.put("canRefund", true);

        if (order.getStatus() == ORDER_REFUNDING) {
            preview.put("canRefund", false);
            preview.put("ruleType", "refunding");
            preview.put("ruleTip", "当前订单已提交退款申请，请等待后台审核处理。");
            return preview;
        }

        if (order.getOrderType() == 2 && order.getStatus() == ORDER_FINISHED) {
            if (order.getFinishTime() == null) {
                preview.put("canRefund", false);
                preview.put("ruleType", "prod_finished_locked");
                preview.put("ruleTip", "当前订单已完成，暂不支持退款。");
                return preview;
            }
            long finishedDays = Duration.between(order.getFinishTime(), LocalDateTime.now()).toDays();
            preview.put("finishedDays", finishedDays);
            if (finishedDays > 7) {
                preview.put("canRefund", false);
                preview.put("ruleType", "prod_finished_expired");
                preview.put("ruleTip", "商品确认收货超过 7 天，当前不可退款。");
                return preview;
            }
            preview.put("ruleType", "prod_finished");
            preview.put("ruleTip", "商品确认收货 7 天内支持申请售后退款，提交后将进入人工审核。");
            return preview;
        }

        if (order.getStatus() != ORDER_PAID && order.getStatus() != ORDER_PROCESSING) {
            preview.put("canRefund", false);
            preview.put("ruleType", "status_locked");
            preview.put("ruleTip", "当前订单状态不可退款。");
            return preview;
        }

        if (order.getOrderType() == 1) {
            Course course = courseService.getById(order.getCourseId());
            if (course != null && course.getType() == 1) {
                UserCoursePackage pkg = userCoursePackageService.getOne(
                        new LambdaQueryWrapper<UserCoursePackage>().eq(UserCoursePackage::getOrderId, order.getId()).last("limit 1")
                );
                int totalLessons = pkg != null && pkg.getTotalLessons() != null && pkg.getTotalLessons() > 0 ? pkg.getTotalLessons() : 1;
                int usedLessons = pkg != null && pkg.getUsedLessons() != null ? pkg.getUsedLessons() : 0;
                BigDecimal lessonPrice = course.getPrice().divide(BigDecimal.valueOf(totalLessons), 2, RoundingMode.HALF_UP);
                BigDecimal estimated = order.getActualAmount()
                        .subtract(lessonPrice.multiply(BigDecimal.valueOf(usedLessons)))
                        .max(BigDecimal.ZERO)
                        .setScale(2, RoundingMode.HALF_UP);
                preview.put("ruleType", "private_package");
                preview.put("usedLessons", usedLessons);
                preview.put("totalLessons", totalLessons);
                preview.put("estimatedRefundAmount", estimated);
                preview.put("ruleTip", "私教课包按已消课节数折算后退款：实付金额 - 已消课节数 x 原价单节价格。退款后剩余课包将失效。");
                return preview;
            }

            if (order.getScheduleId() != null) {
                CourseSchedule schedule = courseScheduleService.getById(order.getScheduleId());
                if (schedule != null) {
                    Course course2 = courseService.getById(order.getCourseId());
                    LocalDateTime scheduleStart = schedule.getStartTime() != null
                            ? schedule.getStartTime() : resolveStartTime(schedule, course2);
                    if (scheduleStart != null) {
                        long minutes = Duration.between(LocalDateTime.now(), scheduleStart).toMinutes();
                        preview.put("minutesBeforeStart", minutes);
                        preview.put("startTime", scheduleStart);
                        if (minutes <= 120) {
                            preview.put("canRefund", false);
                            preview.put("estimatedRefundAmount", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
                            preview.put("ruleType", "group_locked");
                            preview.put("ruleTip", "团课开课前 2 小时内不可退款。");
                            return preview;
                        }
                        if (minutes <= 480) {
                            preview.put("ruleType", "group_penalty");
                            preview.put("estimatedRefundAmount", order.getActualAmount().multiply(BigDecimal.valueOf(0.8)).setScale(2, RoundingMode.HALF_UP));
                            preview.put("ruleTip", "团课开课前 2 到 8 小时内可退款，系统默认扣除 20% 违约金。");
                            return preview;
                        }
                    }
                }
                preview.put("ruleType", "group_free");
                preview.put("ruleTip", "团课开课前 8 小时以上支持免费退款。");
                return preview;
            }
        }

        if (order.getOrderType() == 2) {
            preview.put("ruleType", "prod");
            preview.put("ruleTip", order.getStatus() == ORDER_PAID
                    ? "商品未发货前支持原路退款。"
                    : "商品已进入履约阶段，提交后将进入人工审核。");
        }
        return preview;
    }

    private User requireUser(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    private void ensurePhoneBound(User user) {
        if (!StringUtils.hasText(user.getPhone())) {
            throw new IllegalArgumentException("请先绑定手机号");
        }
    }

    private Order requireOwnedOrder(Long userId, Long orderId) {
        Order order = requireOrder(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在");
        }
        return order;
    }

    private Order requireOrder(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        return order;
    }

    private CouponPricing calculateCouponPricing(Long userId, Long userCouponId, BigDecimal totalAmount, int orderType) {
        if (userCouponId == null) {
            return new CouponPricing(null, BigDecimal.ZERO);
        }
        UserCoupon userCoupon = userCouponService.getById(userCouponId);
        if (userCoupon == null || !userCoupon.getUserId().equals(userId)) {
            throw new IllegalArgumentException("优惠券不存在");
        }
        if (userCoupon.getStatus() != USER_COUPON_UNUSED) {
            throw new IllegalArgumentException("优惠券当前不可用");
        }

        Coupon coupon = couponService.getById(userCoupon.getCouponId());
        if (coupon == null || coupon.getStatus() != 1) {
            throw new IllegalArgumentException("优惠券已失效");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            throw new IllegalArgumentException("优惠券不在有效期内");
        }

        boolean scopeMatch = coupon.getScope() == 1 || (coupon.getScope() == 2 && orderType == 1) || (coupon.getScope() == 3 && orderType == 2);
        if (!scopeMatch) {
            throw new IllegalArgumentException("该优惠券不适用于当前订单");
        }
        if (totalAmount.compareTo(coupon.getMinAmount()) < 0) {
            throw new IllegalArgumentException("订单金额未达到优惠券使用门槛");
        }

        BigDecimal couponAmount = BigDecimal.ZERO;
        if (coupon.getType() == 1 || coupon.getType() == 3) {
            couponAmount = coupon.getDiscount();
        } else if (coupon.getType() == 2) {
            couponAmount = totalAmount.subtract(totalAmount.multiply(coupon.getDiscountRatio()));
        }
        return new CouponPricing(userCouponId, couponAmount.min(totalAmount).setScale(2, RoundingMode.HALF_UP));
    }

    private void lockCouponForOrder(Long userCouponId, Long userId, Long orderId) {
        if (userCouponId == null) {
            return;
        }
        UserCoupon userCoupon = userCouponService.getById(userCouponId);
        if (userCoupon == null || !userCoupon.getUserId().equals(userId)) {
            throw new IllegalArgumentException("优惠券不存在");
        }
        userCoupon.setStatus(USER_COUPON_LOCKED);
        userCoupon.setOrderId(orderId);
        userCoupon.setUseTime(null);
        userCouponService.updateById(userCoupon);
    }

    private void consumeLockedCoupon(Order order) {
        if (order.getCouponId() == null) {
            return;
        }
        UserCoupon userCoupon = userCouponService.getById(order.getCouponId());
        if (userCoupon == null) {
            throw new IllegalArgumentException("优惠券不存在");
        }
        if (userCoupon.getStatus() != USER_COUPON_LOCKED || !order.getId().equals(userCoupon.getOrderId())) {
            throw new IllegalArgumentException("优惠券状态异常，请重新下单");
        }
        userCoupon.setStatus(USER_COUPON_USED);
        userCoupon.setOrderId(order.getId());
        userCoupon.setUseTime(LocalDateTime.now());
        userCouponService.updateById(userCoupon);
    }

    private void unlockCoupon(Order order) {
        if (order.getCouponId() == null) {
            return;
        }
        UserCoupon userCoupon = userCouponService.getById(order.getCouponId());
        if (userCoupon == null || !order.getId().equals(userCoupon.getOrderId())) {
            return;
        }
        userCoupon.setStatus(USER_COUPON_UNUSED);
        userCoupon.setOrderId(null);
        userCoupon.setUseTime(null);
        userCouponService.updateById(userCoupon);
    }

    private void cancelPendingOrder(Order order, String reason) {
        if (order.getStatus() != ORDER_PENDING) {
            throw new IllegalArgumentException("只能取消待支付订单");
        }
        order.setStatus(ORDER_CANCELED);
        order.setCloseTime(LocalDateTime.now());
        if (StringUtils.hasText(reason)) {
            order.setRefundReason(reason);
        }
        this.updateById(order);
        unlockCoupon(order);
    }

    private void doPay(Order order) {
        RLock orderLock = redissonClient.getLock("order:pay:" + order.getId());
        orderLock.lock();
        try {
            Order latest = requireOrder(order.getId());
            if (latest.getStatus() != ORDER_PENDING) {
                throw new IllegalArgumentException("订单状态异常");
            }

            if (latest.getOrderType() == 1) {
                handleCoursePay(latest);
            } else {
                handleProdPay(latest);
            }

            latest.setPaymentTime(LocalDateTime.now());
            this.updateById(latest);
            consumeLockedCoupon(latest);
            if (latest.getOrderType() == 1 && latest.getScheduleId() != null) {
                publishGroupCheckEventIfAbsent(latest.getScheduleId());
            }
            txEventHelper.afterCommit(() -> orderEventPublisher.publishOrderPaid(toLifecycleMessage(latest)));
        } finally {
            orderLock.unlock();
        }
    }

    private void handleCoursePay(Order order) {
        Course course = courseService.getById(order.getCourseId());
        if (course == null || course.getStatus() != 1) {
            throw new IllegalArgumentException("课程不存在或已下架");
        }

        if (course.getType() == 1) {
            order.setStatus(ORDER_PROCESSING);

            UserCoursePackage pkg = new UserCoursePackage();
            pkg.setUserId(order.getUserId());
            pkg.setCourseId(course.getId());
            pkg.setOrderId(order.getId());
            pkg.setTotalLessons(course.getLessonCount() != null ? course.getLessonCount() : 1);
            pkg.setUsedLessons(0);
            int validityDays = course.getValidityDays() != null ? course.getValidityDays() : 90;
            pkg.setExpireTime(LocalDateTime.now().plusDays(validityDays));
            pkg.setStatus(1);
            userCoursePackageService.save(pkg);

            course.setSales(defaultInt(course.getSales()) + 1);
            courseService.updateById(course);
            return;
        }

        if (order.getScheduleId() == null) {
            throw new IllegalArgumentException("团课订单缺少排课信息");
        }
        RLock scheduleLock = redissonClient.getLock("order:schedule:" + order.getScheduleId());
        scheduleLock.lock();
        try {
            CourseSchedule schedule = courseScheduleService.getById(order.getScheduleId());
            if (schedule == null || schedule.getStatus() != 0) {
                throw new IllegalArgumentException("该排课当前无法支付");
            }
            if (schedule.getStartTime() != null && !schedule.getStartTime().isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("该场次已开始，无法支付");
            }
            if (schedule.getEnrolledSeats() >= schedule.getTotalSeats()) {
                throw new IllegalArgumentException("该排课名额已满");
            }
            schedule.setEnrolledSeats(schedule.getEnrolledSeats() + 1);
            courseScheduleService.updateById(schedule);
        } finally {
            scheduleLock.unlock();
        }

        order.setStatus(ORDER_PROCESSING);
        course.setSales(defaultInt(course.getSales()) + 1);
        courseService.updateById(course);
    }

    private void handleProdPay(Order order) {
        List<OrderItem> items = orderItemService.list(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        if (items.isEmpty()) {
            throw new IllegalArgumentException("订单商品不存在");
        }

        for (OrderItem item : items) {
            Prod prod = prodService.getById(item.getItemId());
            if (prod == null || prod.getStatus() != 1) {
                throw new IllegalArgumentException("商品不存在或已下架");
            }
            if (item.getSkuId() != null) {
                RLock skuLock = redissonClient.getLock("order:sku:" + item.getSkuId());
                skuLock.lock();
                try {
                    Sku sku = skuService.getById(item.getSkuId());
                    if (sku == null) {
                        throw new IllegalArgumentException("商品规格不存在");
                    }
                    if (sku.getStocks() < item.getQty()) {
                        throw new IllegalArgumentException("商品库存不足");
                    }
                    sku.setStocks(sku.getStocks() - item.getQty());
                    skuService.updateById(sku);
                } finally {
                    skuLock.unlock();
                }
            }
            prod.setSales(Math.max(0, defaultInt(prod.getSales())) + item.getQty());
            prodService.updateById(prod);
        }

        order.setStatus(ORDER_PAID);
    }

    private void finalizeRefund(Order order, boolean updateRefundReason) {
        Order latest = requireOrder(order.getId());
        BigDecimal refundAmount = latest.getActualAmount() == null ? BigDecimal.ZERO : latest.getActualAmount();
        boolean restoreCoupon = true;

        if (latest.getOrderType() == 1) {
            Course course = courseService.getById(latest.getCourseId());
            if (course != null && course.getType() == 1) {
                UserCoursePackage pkg = userCoursePackageService.getOne(
                        new LambdaQueryWrapper<UserCoursePackage>().eq(UserCoursePackage::getOrderId, latest.getId()).last("limit 1")
                );
                if (pkg != null) {
                    int totalLessons = pkg.getTotalLessons() == null || pkg.getTotalLessons() <= 0 ? 1 : pkg.getTotalLessons();
                    int usedLessons = pkg.getUsedLessons() == null ? 0 : pkg.getUsedLessons();
                    BigDecimal lessonPrice = course.getPrice().divide(BigDecimal.valueOf(totalLessons), 2, RoundingMode.HALF_UP);
                    BigDecimal usedAmount = lessonPrice.multiply(BigDecimal.valueOf(usedLessons)).setScale(2, RoundingMode.HALF_UP);
                    refundAmount = latest.getActualAmount().subtract(usedAmount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
                    pkg.setStatus(2);
                    userCoursePackageService.updateById(pkg);
                    restoreCoupon = refundAmount.compareTo(latest.getActualAmount()) == 0;
                    if (usedLessons == 0) {
                        course.setSales(Math.max(0, defaultInt(course.getSales()) - 1));
                        courseService.updateById(course);
                    }
                }
            } else if (latest.getScheduleId() != null) {
                CourseSchedule schedule = courseScheduleService.getById(latest.getScheduleId());
                if (schedule != null && schedule.getStartTime() != null) {
                    long minutes = Duration.between(LocalDateTime.now(), schedule.getStartTime()).toMinutes();
                    if (minutes > 120 && minutes <= 480) {
                        refundAmount = latest.getActualAmount()
                                .multiply(BigDecimal.valueOf(0.8))
                                .setScale(2, RoundingMode.HALF_UP);
                    }
                }
                RLock scheduleLock = redissonClient.getLock("order:schedule:" + latest.getScheduleId());
                scheduleLock.lock();
                try {
                    schedule = courseScheduleService.getById(latest.getScheduleId());
                    if (schedule != null && schedule.getEnrolledSeats() > 0) {
                        schedule.setEnrolledSeats(schedule.getEnrolledSeats() - 1);
                        courseScheduleService.updateById(schedule);
                    }
                } finally {
                    scheduleLock.unlock();
                }
                if (course != null) {
                    course.setSales(Math.max(0, defaultInt(course.getSales()) - 1));
                    courseService.updateById(course);
                }
            }
        } else {
            List<OrderItem> items = orderItemService.list(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, latest.getId()));
            for (OrderItem item : items) {
                Prod prod = prodService.getById(item.getItemId());
                if (prod != null) {
                    prod.setSales(Math.max(0, defaultInt(prod.getSales()) - item.getQty()));
                    prodService.updateById(prod);
                }
                if (item.getSkuId() != null) {
                    RLock skuLock = redissonClient.getLock("order:sku:" + item.getSkuId());
                    skuLock.lock();
                    try {
                        Sku sku = skuService.getById(item.getSkuId());
                        if (sku != null) {
                            sku.setStocks(defaultInt(sku.getStocks()) + item.getQty());
                            skuService.updateById(sku);
                        }
                    } finally {
                        skuLock.unlock();
                    }
                }
            }
        }

        latest.setStatus(ORDER_REFUNDED);
        latest.setRefundAmount(refundAmount);
        latest.setCloseTime(LocalDateTime.now());
        latest.setBeforeRefundStatus(null);
        if (updateRefundReason && !StringUtils.hasText(latest.getRefundReason())) {
            latest.setRefundReason("退款成功");
        }
        this.updateById(latest);

        if (restoreCoupon) {
            unlockCoupon(latest);
        }

        Order eventOrder = latest;
        txEventHelper.afterCommit(() -> orderEventPublisher.publishOrderRefunded(toLifecycleMessage(eventOrder)));
    }

    private List<ProductLine> buildProductLines(Long userId, Order params) {
        List<ProductLine> lines = new ArrayList<>();
        if (params.getCartIds() != null && !params.getCartIds().isEmpty()) {
            List<Cart> cartList = cartService.list(new LambdaQueryWrapper<Cart>()
                    .eq(Cart::getUserId, userId)
                    .in(Cart::getId, params.getCartIds()));
            if (cartList.size() != params.getCartIds().size()) {
                // 找出哪些购物车ID不存在或不属于当前用户
                List<Long> foundIds = cartList.stream().map(Cart::getId).toList();
                List<Long> missingIds = params.getCartIds().stream()
                        .filter(id -> !foundIds.contains(id))
                        .toList();
                throw new IllegalArgumentException("购物车数据异常，请刷新后重试。无效的购物车ID: " + missingIds);
            }
            for (Cart cart : cartList) {
                lines.add(buildLine(cart.getProdId(), cart.getSkuId(), cart.getQty()));
            }
            return lines;
        }

        if (params.getProdId() != null) {
            int qty = params.getBuyQty() == null || params.getBuyQty() <= 0 ? 1 : params.getBuyQty();
            lines.add(buildLine(params.getProdId(), params.getSkuId(), qty));
            return lines;
        }

        List<Cart> cartList = cartService.list(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
        for (Cart cart : cartList) {
            lines.add(buildLine(cart.getProdId(), cart.getSkuId(), cart.getQty()));
        }
        return lines;
    }

    private ProductLine buildLine(Long prodId, Long skuId, Integer qty) {
        if (qty == null || qty <= 0) {
            throw new IllegalArgumentException("商品数量不合法");
        }
        Prod prod = prodService.getById(prodId);
        if (prod == null || prod.getStatus() != 1) {
            throw new IllegalArgumentException("商品不存在或已下架");
        }
        Sku sku = null;
        BigDecimal price = prod.getPrice();
        if (skuId != null) {
            sku = skuService.getById(skuId);
            if (sku == null || !prodId.equals(sku.getProdId())) {
                throw new IllegalArgumentException("商品规格不存在");
            }
            if (sku.getStocks() < qty) {
                throw new IllegalArgumentException("商品库存不足");
            }
            price = sku.getPrice();
        }
        return new ProductLine(prod, sku, price, qty);
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String appendRemark(String remark, String extra) {
        if (!StringUtils.hasText(remark)) {
            return extra;
        }
        if (remark.contains(extra)) {
            return remark;
        }
        return remark + "；" + extra;
    }

    private OrderLifecycleMessage toLifecycleMessage(Order order) {
        return new OrderLifecycleMessage(order.getId(), order.getOrderType(), order.getStatus(), order.getOrderNumber());
    }

    private void publishGroupCheckEventIfAbsent(Long scheduleId) {
        CourseSchedule schedule = courseScheduleService.getById(scheduleId);
        // 优先用scheduleDate+课程模板时间段合成；兼容旧数据使用startTime
        LocalDateTime scheduleStart = null;
        if (schedule != null) {
            Course course = courseService.getById(schedule.getCourseId());
            scheduleStart = resolveStartTime(schedule, course);
            if (scheduleStart == null) scheduleStart = schedule.getStartTime();
        }
        if (scheduleStart == null) {
            return;
        }

        LocalDateTime checkTime = scheduleStart.minusMinutes(orderBizProperties.getGroupCheckBeforeMinutes());
        long delayMillis = Math.max(Duration.between(LocalDateTime.now(), checkTime).toMillis(), 1000L);
        long ttlSeconds = Math.max(Duration.between(LocalDateTime.now(), scheduleStart).getSeconds(), 60L);

        RBucket<String> bucket = redissonClient.getBucket("order:group:check:scheduled:" + scheduleId);
        boolean firstSchedule = Boolean.TRUE.equals(bucket.trySet("1", ttlSeconds, TimeUnit.SECONDS));
        if (firstSchedule) {
            orderEventPublisher.publishGroupCheckDelay(scheduleId, delayMillis);
        }
    }

    private String generateOrderNumber() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        String random = String.format("%06d", new Random().nextInt(1000000));
        return "KS" + timestamp + random;
    }

    private record CouponPricing(Long userCouponId, BigDecimal couponAmount) {
    }

    /**
     * 将排课日期 + 课程模板时间段合成 LocalDateTime
     * 优先用 scheduleDate + course.startHour，兼容旧数据的 startTime
     */
    private LocalDateTime resolveStartTime(CourseSchedule schedule, Course course) {
        if (schedule == null) return null;
        if (schedule.getScheduleDate() != null && course != null
                && course.getStartHour() != null && !course.getStartHour().isEmpty()) {
            try {
                LocalTime time = LocalTime.parse(course.getStartHour());
                return LocalDateTime.of(schedule.getScheduleDate(), time);
            } catch (Exception ignored) {}
        }
        return schedule.getStartTime();
    }

    /**
     * 将排课日期 + 课程模板时间段合成结束 LocalDateTime
     */
    private LocalDateTime resolveEndTime(CourseSchedule schedule, Course course) {
        if (schedule == null) return null;
        if (schedule.getScheduleDate() != null && course != null
                && course.getEndHour() != null && !course.getEndHour().isEmpty()) {
            try {
                LocalTime time = LocalTime.parse(course.getEndHour());
                return LocalDateTime.of(schedule.getScheduleDate(), time);
            } catch (Exception ignored) {}
        }
        return schedule.getEndTime();
    }

    @Getter
    private static class ProductLine {
        private final Prod prod;
        private final Sku sku;
        private final BigDecimal price;
        private final Integer qty;

        private ProductLine(Prod prod, Sku sku, BigDecimal price, Integer qty) {
            this.prod = prod;
            this.sku = sku;
            this.price = price;
            this.qty = qty;
        }
    }
}
