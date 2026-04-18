package com.kinetic.sports.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kinetic.sports.bean.model.*;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final CourseService courseService;
    private final CoachService coachService;
    private final OrderService orderService;
    private final CourseCheckinService courseCheckinService;

    @GetMapping("/stats")
    public ServerResponseEntity<Map<String, Object>> stats() {
        Map<String, Object> result = new HashMap<>();

        // 用户总数
        long totalUsers = userService.count();
        result.put("totalUsers", totalUsers);

        // 课程总数（上架）
        long totalCourses = courseService.count(
                new LambdaQueryWrapper<Course>().eq(Course::getStatus, 1)
        );
        result.put("totalCourses", totalCourses);

        // 教练总数（在职）
        long totalCoaches = coachService.count(
                new LambdaQueryWrapper<Coach>().eq(Coach::getStatus, 1)
        );
        result.put("totalCoaches", totalCoaches);

        // 今日新增订单数
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        long todayOrders = orderService.count(
                new LambdaQueryWrapper<Order>()
                        .between(Order::getCreateTime, todayStart, todayEnd)
        );
        result.put("todayOrders", todayOrders);

        // 今日收入（已完成订单）
        List<Order> todayFinished = orderService.list(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, 4)
                        .between(Order::getFinishTime, todayStart, todayEnd)
        );
        BigDecimal todayIncome = todayFinished.stream()
                .map(o -> o.getActualAmount() != null ? o.getActualAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("todayIncome", todayIncome);

        // 待处理退款数
        long pendingRefunds = orderService.count(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, 6)
        );
        result.put("pendingRefunds", pendingRefunds);

        // 本月消课次数
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long monthCheckins = courseCheckinService.count(
                new LambdaQueryWrapper<CourseCheckin>()
                        .eq(CourseCheckin::getStatus, 1)
                        .ge(CourseCheckin::getCheckinTime, monthStart)
        );
        result.put("monthCheckins", monthCheckins);

        // 待结算消课数
        long unsettledCheckins = courseCheckinService.count(
                new LambdaQueryWrapper<CourseCheckin>()
                        .eq(CourseCheckin::getSettleStatus, 0)
                        .eq(CourseCheckin::getStatus, 1)
        );
        result.put("unsettledCheckins", unsettledCheckins);

        // 近 7 天每日订单数
        List<Map<String, Object>> dailyOrders = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            long count = orderService.count(
                    new LambdaQueryWrapper<Order>()
                            .between(Order::getCreateTime, start, end)
            );
            Map<String, Object> day = new HashMap<>();
            day.put("date", date.toString().substring(5)); // MM-dd
            day.put("count", count);
            dailyOrders.add(day);
        }
        result.put("dailyOrders", dailyOrders);

        return ServerResponseEntity.success(result);
    }
}
