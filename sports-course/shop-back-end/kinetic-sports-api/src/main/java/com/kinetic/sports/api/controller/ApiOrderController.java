package com.kinetic.sports.api.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.bean.model.Order;
import com.kinetic.sports.bean.model.OrderItem;
import com.kinetic.sports.bean.model.User;
import com.kinetic.sports.bean.model.Course;
import com.kinetic.sports.bean.model.CourseSchedule;
import com.kinetic.sports.bean.model.Coach;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.OrderItemService;
import com.kinetic.sports.service.OrderService;
import com.kinetic.sports.service.UserService;
import com.kinetic.sports.service.CourseService;
import com.kinetic.sports.service.CourseScheduleService;
import com.kinetic.sports.service.CoachService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class ApiOrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final UserService userService;
    private final CourseService courseService;
    private final CourseScheduleService courseScheduleService;
    private final CoachService coachService;

    @PostMapping("/course")
    public ServerResponseEntity<Order> createCourseOrder(@RequestBody Order order) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ServerResponseEntity.success(orderService.createCourseOrder(userId, order));
    }

    @PostMapping("/course/batch")
    public ServerResponseEntity<List<Order>> createBatchCourseOrder(@RequestBody Order order) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ServerResponseEntity.success(orderService.createBatchCourseOrders(userId, order));
    }

    @GetMapping("/list")
    public ServerResponseEntity<Page<Order>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer orderType,
            @RequestParam(required = false) Integer status) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (orderType != null) {
            wrapper.eq(Order::getOrderType, orderType);
        }
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        return ServerResponseEntity.success(orderService.page(page, wrapper));
    }

    @GetMapping("/detail/{id}")
    public ServerResponseEntity<Map<String, Object>> detail(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        Order order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return ServerResponseEntity.fail("订单不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);

        // 课程订单：返回课程和教练信息
        if (order.getOrderType() == 1 && order.getCourseId() != null) {
            Course course = courseService.getById(order.getCourseId());
            result.put("course", course);
            // 团课排课信息
            Long coachId = course != null ? course.getCoachId() : null;
            if (order.getScheduleId() != null) {
                CourseSchedule schedule = courseScheduleService.getById(order.getScheduleId());
                result.put("schedule", schedule);
                if (schedule != null && schedule.getCoachId() != null) {
                    coachId = schedule.getCoachId();
                }
            }
            // 获取教练信息
            if (coachId != null) {
                Coach coach = coachService.getById(coachId);
                result.put("coach", coach);
            }
        }

        return ServerResponseEntity.success(result);
    }

    @GetMapping("/items/{orderId}")
    public ServerResponseEntity<List<OrderItem>> orderItems(@PathVariable Long orderId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Order order = orderService.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return ServerResponseEntity.fail("订单不存在");
        }
        return ServerResponseEntity.success(orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        ));
    }

    @GetMapping("/refund-preview/{id}")
    public ServerResponseEntity<Map<String, Object>> refundPreview(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ServerResponseEntity.success(orderService.previewRefund(userId, id));
    }

    @PutMapping("/cancel/{id}")
    public ServerResponseEntity<Void> cancel(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        orderService.cancelOrder(userId, id);
        return ServerResponseEntity.success();
    }

    @PutMapping("/refund/{id}")
    public ServerResponseEntity<Void> refund(@PathVariable Long id, @RequestBody Order params) {
        Long userId = StpUtil.getLoginIdAsLong();
        orderService.applyRefund(userId, id, params.getRefundReason());
        return ServerResponseEntity.success();
    }

    @PutMapping("/pay/{id}")
    public ServerResponseEntity<Void> pay(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        orderService.payOrder(userId, id);
        return ServerResponseEntity.success();
    }

    @PutMapping("/pay/batch")
    public ServerResponseEntity<Void> batchPay(@RequestBody Order params) {
        Long userId = StpUtil.getLoginIdAsLong();
        orderService.payBatchOrders(userId, params.getOrderIds());
        return ServerResponseEntity.success();
    }

    @GetMapping("/schedule/{scheduleId}/attendees")
    public ServerResponseEntity<List<Map<String, Object>>> getScheduleAttendees(@PathVariable Long scheduleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Order selfOrder = orderService.getOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .eq(Order::getScheduleId, scheduleId)
                        .in(Order::getStatus, 2, 3, 4)
                        .last("limit 1")
        );
        if (selfOrder == null) {
            return ServerResponseEntity.fail("无权查看该场次报名信息");
        }

        List<Map<String, Object>> result = new ArrayList<>();
        User user = userService.getById(userId);
        if (user != null) {
            Map<String, Object> item = new HashMap<>();
            item.put("userId", user.getId());
            item.put("nickName", user.getNickName());
            item.put("avatarUrl", user.getAvatarUrl());
            result.add(item);
        }
        return ServerResponseEntity.success(result);
    }
}
