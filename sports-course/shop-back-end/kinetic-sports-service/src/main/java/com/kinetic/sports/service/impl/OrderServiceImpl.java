package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.dto.order.OrderLifecycleMessage;
import com.kinetic.sports.bean.model.Course;
import com.kinetic.sports.bean.model.CourseSchedule;
import com.kinetic.sports.bean.model.Coupon;
import com.kinetic.sports.bean.model.Order;
import com.kinetic.sports.bean.model.OrderItem;
import com.kinetic.sports.bean.model.User;
import com.kinetic.sports.bean.model.UserCoupon;
import com.kinetic.sports.bean.model.UserCoursePackage;
import com.kinetic.sports.common.config.OrderBizProperties;
import com.kinetic.sports.service.CourseScheduleService;
import com.kinetic.sports.service.CourseService;
import com.kinetic.sports.service.CouponService;
import com.kinetic.sports.service.OrderItemService;
import com.kinetic.sports.service.OrderService;
import com.kinetic.sports.service.UserCouponService;
import com.kinetic.sports.service.UserCoursePackageService;
import com.kinetic.sports.service.UserService;
import com.kinetic.sports.service.mapper.OrderMapper;
import com.kinetic.sports.service.mq.OrderEventPublisher;
import com.kinetic.sports.service.mq.TxEventHelper;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createCourseOrder(Long userId, Order params) {
        User currentUser = requireUser(userId);
        ensurePhoneBound(currentUser);

        Course course = courseService.getById(params.getCourseId());
        if (course == null || course.getStatus() != 1) {
            throw new IllegalArgumentException("课程不存在或已下架");
        }

        RLock userScheduleLock = null;
        try {
            Order order = new Order();
            order.setUserId(userId);
            order.setCourseId(course.getId());
            order.setOrderType(1);
            order.setStatus(ORDER_PENDING);
            order.setOrderNumber(generateOrderNumber());
            order.setRemark(params.getRemark());

            if (Objects.equals(course.getType(), 2)) {
                // 团课订单必须绑定具体场次；这里先锁住“用户+场次”维度，避免重复下单。
                if (params.getScheduleId() == null) {
                    throw new IllegalArgumentException("请选择上课时间");
                }
                userScheduleLock = redissonClient.getLock("order:user:schedule:create:" + userId + ":" + params.getScheduleId());
                userScheduleLock.lock();
                CourseSchedule schedule = courseScheduleService.getById(params.getScheduleId());
                validateScheduleForSignup(course, schedule);
                long existCount = this.count(new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .eq(Order::getScheduleId, params.getScheduleId())
                        .in(Order::getStatus, ORDER_PENDING, ORDER_PAID, ORDER_PROCESSING, ORDER_FINISHED, ORDER_REFUNDING));
                if (existCount > 0) {
                    throw new IllegalArgumentException("您已报名该场次，请勿重复购买");
                }
                order.setScheduleId(params.getScheduleId());
            }

            // 私教和团课共用订单模型，但优惠券、金额计算在创建阶段统一处理。
            BigDecimal totalAmount = course.getPrice();
            CouponPricing pricing = calculateCouponPricing(userId, params.getCouponId(), totalAmount);
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
            // 订单创建成功后发送延迟关单消息，避免待支付订单长期占用资源。
            txEventHelper.afterCommit(() -> orderEventPublisher.publishOrderCloseDelay(order.getId()));
            return order;
        } finally {
            if (userScheduleLock != null && userScheduleLock.isHeldByCurrentThread()) {
                userScheduleLock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Order> createBatchCourseOrders(Long userId, Order params) {
        if (params.getScheduleIds() == null || params.getScheduleIds().isEmpty()) {
            throw new IllegalArgumentException("请选择至少一个上课时间");
        }
        if (params.getCouponId() != null) {
            throw new IllegalArgumentException("多场次报名暂不支持使用优惠券");
        }

        List<Order> orders = new ArrayList<>();
        for (Long scheduleId : params.getScheduleIds()) {
            Order single = new Order();
            single.setCourseId(params.getCourseId());
            single.setScheduleId(scheduleId);
            single.setRemark(params.getRemark());
            orders.add(createCourseOrder(userId, single));
        }
        return orders;
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
    public void payBatchOrders(Long userId, List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            throw new IllegalArgumentException("请选择要支付的订单");
        }
        for (Long orderId : orderIds) {
            Order order = requireOwnedOrder(userId, orderId);
            doPay(order);
        }
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
        if (order.getStatus() != ORDER_PAID && order.getStatus() != ORDER_PROCESSING) {
            throw new IllegalArgumentException("当前订单状态无法退款");
        }

        if (order.getCourseId() != null) {
            Course course = courseService.getById(order.getCourseId());
            if (course != null && Objects.equals(course.getType(), 2) && order.getScheduleId() != null) {
                CourseSchedule schedule = courseScheduleService.getById(order.getScheduleId());
                if (schedule != null) {
                    LocalDateTime scheduleStart = resolveStartTime(schedule, course);
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
        order.setStatus(order.getBeforeRefundStatus() == null ? ORDER_PROCESSING : order.getBeforeRefundStatus());
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
            int minGroupSize = course != null && course.getMinGroupSize() != null ? course.getMinGroupSize() : 1;
            long enrolledCount = this.count(new LambdaQueryWrapper<Order>()
                    .eq(Order::getScheduleId, scheduleId)
                    .in(Order::getStatus, ORDER_PAID, ORDER_PROCESSING, ORDER_FINISHED));

            Map<String, Object> result = new HashMap<>();
            result.put("scheduleId", scheduleId);
            result.put("enrolledCount", enrolledCount);
            result.put("minGroupSize", minGroupSize);

            if (Objects.equals(schedule.getStatus(), 2)) {
                result.put("grouped", enrolledCount >= minGroupSize);
                result.put("message", "排课已结课");
                return result;
            }
            if (Objects.equals(schedule.getStatus(), 3)) {
                result.put("grouped", false);
                result.put("message", "排课已取消");
                return result;
            }

            LocalDateTime scheduleStart = resolveStartTime(schedule, course);
            LocalDateTime checkTime = scheduleStart == null
                    ? null
                    : scheduleStart.minusMinutes(orderBizProperties.getGroupCheckBeforeMinutes());
            boolean canFinalizeGroup = checkTime == null || !LocalDateTime.now().isBefore(checkTime);

            if (enrolledCount >= minGroupSize) {
                // 达到成团人数后，场次进入可履约状态；当前配置下通常在开课时触发。
                if (Objects.equals(schedule.getStatus(), 0)) {
                    schedule.setStatus(1);
                    courseScheduleService.updateById(schedule);
                }
                result.put("grouped", true);
                result.put("message", canFinalizeGroup
                        ? "已成团，共 " + enrolledCount + " 人报名"
                        : "当前已达到成团人数，共 " + enrolledCount + " 人报名");
                return result;
            }

            if (!canFinalizeGroup) {
                result.put("grouped", false);
                result.put("message", "未到成团判断时间，当前 " + enrolledCount + " 人，需 " + minGroupSize + " 人");
                return result;
            }

            if (!Objects.equals(schedule.getStatus(), 3)) {
                // 到了成团判断时间仍未成团，则取消场次并回滚相关订单。
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

        if (order.getStatus() != ORDER_PAID && order.getStatus() != ORDER_PROCESSING) {
            preview.put("canRefund", false);
            preview.put("ruleType", "status_locked");
            preview.put("ruleTip", "当前订单状态不可退款。");
            return preview;
        }

        Course course = courseService.getById(order.getCourseId());
        if (course != null && Objects.equals(course.getType(), 1)) {
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
                LocalDateTime scheduleStart = resolveStartTime(schedule, course);
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
        }
        return preview;
    }

    private void validateScheduleForSignup(Course course, CourseSchedule schedule) {
        if (schedule == null) {
            throw new IllegalArgumentException("所选排课不存在或已无法报名");
        }
        if (course == null || !Objects.equals(schedule.getCourseId(), course.getId())) {
            throw new IllegalArgumentException("排课与课程不匹配，请重新选择");
        }
        if (schedule.getStatus() == null || (schedule.getStatus() != 0 && schedule.getStatus() != 1)) {
            throw new IllegalArgumentException("所选排课不存在或已无法报名");
        }
        LocalDateTime scheduleStart = resolveStartTime(schedule, course);
        if (scheduleStart != null && !scheduleStart.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("该场次已开始，无法报名");
        }
        if (defaultInt(schedule.getEnrolledSeats()) >= defaultInt(schedule.getTotalSeats())) {
            throw new IllegalArgumentException("该排课名额已满");
        }
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

    private CouponPricing calculateCouponPricing(Long userId, Long userCouponId, BigDecimal totalAmount) {
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

        boolean scopeMatch = coupon.getScope() == 1 || coupon.getScope() == 2;
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
            if (!Objects.equals(latest.getOrderType(), 1)) {
                throw new IllegalArgumentException("课程版仅支持课程订单支付");
            }

            handleCoursePay(latest);

            latest.setPaymentTime(LocalDateTime.now());
            this.updateById(latest);
            consumeLockedCoupon(latest);
            if (latest.getScheduleId() != null) {
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

        if (Objects.equals(course.getType(), 1)) {
            // 私教课支付成功后不是占用场次，而是生成后续可销课的课包。
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
        // 团课支付成功后才真正占用名额，因此这里对场次加锁并递增已报名人数。
        RLock scheduleLock = redissonClient.getLock("order:schedule:" + order.getScheduleId());
        scheduleLock.lock();
        try {
            CourseSchedule schedule = courseScheduleService.getById(order.getScheduleId());
            validateScheduleForSignup(course, schedule);
            long duplicatePaidCount = this.count(new LambdaQueryWrapper<Order>()
                    .eq(Order::getUserId, order.getUserId())
                    .eq(Order::getScheduleId, order.getScheduleId())
                    .ne(Order::getId, order.getId())
                    .in(Order::getStatus, ORDER_PAID, ORDER_PROCESSING, ORDER_FINISHED, ORDER_REFUNDING));
            if (duplicatePaidCount > 0) {
                throw new IllegalArgumentException("您已报名该场次，请勿重复支付");
            }
            schedule.setEnrolledSeats(defaultInt(schedule.getEnrolledSeats()) + 1);
            courseScheduleService.updateById(schedule);
        } finally {
            scheduleLock.unlock();
        }

        order.setStatus(ORDER_PROCESSING);
        course.setSales(defaultInt(course.getSales()) + 1);
        courseService.updateById(course);
    }

    private void finalizeRefund(Order order, boolean updateRefundReason) {
        Order latest = requireOrder(order.getId());
        BigDecimal refundAmount = latest.getActualAmount() == null ? BigDecimal.ZERO : latest.getActualAmount();
        boolean restoreCoupon = true;

        Course course = courseService.getById(latest.getCourseId());
        if (course != null && Objects.equals(course.getType(), 1)) {
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
            // 团课退款需要同时回滚场次人数和课程销量，保证排课数据一致。
            CourseSchedule schedule = courseScheduleService.getById(latest.getScheduleId());
            LocalDateTime scheduleStart = resolveStartTime(schedule, course);
            if (scheduleStart != null) {
                long minutes = Duration.between(LocalDateTime.now(), scheduleStart).toMinutes();
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
                if (schedule != null && defaultInt(schedule.getEnrolledSeats()) > 0) {
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

        txEventHelper.afterCommit(() -> orderEventPublisher.publishOrderRefunded(toLifecycleMessage(latest)));
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
        LocalDateTime scheduleStart = null;
        if (schedule != null) {
            Course course = courseService.getById(schedule.getCourseId());
            scheduleStart = resolveStartTime(schedule, course);
            if (scheduleStart == null) {
                scheduleStart = schedule.getStartTime();
            }
        }
        if (scheduleStart == null) {
            return;
        }

        // 通过“开课时间 - 提前分钟数”计算成团检查时刻；当前配置为 0，表示开课时判断。
        LocalDateTime checkTime = scheduleStart.minusMinutes(orderBizProperties.getGroupCheckBeforeMinutes());
        long delayMillis = Math.max(Duration.between(LocalDateTime.now(), checkTime).toMillis(), 1000L);
        long ttlSeconds = Math.max(Duration.between(LocalDateTime.now(), scheduleStart).getSeconds(), 60L);

        // 同一场次只投递一次成团检查消息，避免每次支付都重复入队。
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

    private LocalDateTime resolveStartTime(CourseSchedule schedule, Course course) {
        if (schedule == null) {
            return null;
        }
        if (schedule.getScheduleDate() != null && course != null
                && StringUtils.hasText(course.getStartHour())) {
            try {
                LocalTime time = LocalTime.parse(course.getStartHour());
                return LocalDateTime.of(schedule.getScheduleDate(), time);
            } catch (Exception ignored) {
                return schedule.getStartTime();
            }
        }
        return schedule.getStartTime();
    }

    private record CouponPricing(Long userCouponId, BigDecimal couponAmount) {
    }
}
