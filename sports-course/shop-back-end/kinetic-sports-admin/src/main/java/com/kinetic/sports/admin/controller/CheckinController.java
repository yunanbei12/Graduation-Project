package com.kinetic.sports.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.bean.dto.checkin.PrivateCheckinCreateDTO;
import com.kinetic.sports.bean.model.*;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/checkin")
@RequiredArgsConstructor
public class CheckinController {

    private final CourseCheckinService courseCheckinService;
    private final UserCoursePackageService userCoursePackageService;
    private final CourseService courseService;
    private final CourseScheduleService courseScheduleService;
    private final OrderService orderService;
    private final UserService userService;
    private final CoachService coachService;

    /**
     * 私教课消课
     */
    @PostMapping("/private")
    public ServerResponseEntity<Void> privateCheckin(@RequestBody PrivateCheckinCreateDTO request) {
        if (request.getUserId() == null || request.getCourseId() == null || request.getCoachId() == null || request.getCheckinTime() == null) {
            return ServerResponseEntity.fail("请选择学员、课程、教练和上课时间");
        }
        LocalDateTime now = LocalDateTime.now();
        if (request.getCheckinTime().isAfter(now)) {
            return ServerResponseEntity.fail("上课时间不能晚于当前时间");
        }

        Course course = courseService.getById(request.getCourseId());
        if (course == null || course.getType() == null || course.getType() != 1) {
            return ServerResponseEntity.fail("私教课程不存在");
        }

        Coach coach = coachService.getById(request.getCoachId());
        if (coach == null || coach.getStatus() == null || coach.getStatus() != 1) {
            return ServerResponseEntity.fail("教练不存在或已停用");
        }

        List<UserCoursePackage> packages = userCoursePackageService.list(
                new LambdaQueryWrapper<UserCoursePackage>()
                        .eq(UserCoursePackage::getUserId, request.getUserId())
                        .eq(UserCoursePackage::getCourseId, request.getCourseId())
                        .eq(UserCoursePackage::getStatus, 1)
                        .orderByAsc(UserCoursePackage::getExpireTime)
                        .orderByAsc(UserCoursePackage::getCreateTime)
        );
        syncExpiredPackages(packages);

        UserCoursePackage pkg = packages.stream()
                .filter(item -> item.getUsedLessons() != null
                        && item.getTotalLessons() != null
                        && !isPackageExpired(item, now)
                        && item.getUsedLessons() < item.getTotalLessons())
                .findFirst()
                .orElse(null);

        if (pkg == null) {
            return ServerResponseEntity.fail("该学员当前课程暂无可用课包");
        }

        BigDecimal lessonPrice = pkg.getTotalLessons() > 0
                ? course.getPrice().divide(BigDecimal.valueOf(pkg.getTotalLessons()), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal coachRatio = course.getSettleRatio() != null ? course.getSettleRatio() : BigDecimal.ZERO;

        CourseCheckin checkin = new CourseCheckin();
        checkin.setPackageId(pkg.getId());
        checkin.setUserId(pkg.getUserId());
        checkin.setCourseId(pkg.getCourseId());
        checkin.setCoachId(request.getCoachId());
        checkin.setCheckinTime(request.getCheckinTime());
        checkin.setCheckinType(1);
        checkin.setStatus(1);
        checkin.setLessonPrice(lessonPrice);
        checkin.setCoachRatio(coachRatio);
        checkin.setCoachAmount(lessonPrice.multiply(coachRatio).setScale(2, RoundingMode.HALF_UP));
        checkin.setSettleStatus(0);
        checkin.setRemark(request.getRemark());
        courseCheckinService.save(checkin);

        // 更新课包已用节数
        pkg.setUsedLessons(pkg.getUsedLessons() + 1);
        boolean isCompleted = pkg.getUsedLessons() >= pkg.getTotalLessons();
        if (isCompleted) {
            pkg.setStatus(3); // 已完成
        }
        userCoursePackageService.updateById(pkg);

        // 如果课包已完成，将对应订单状态更新为已完成
        if (isCompleted && pkg.getOrderId() != null) {
            Order order = orderService.getById(pkg.getOrderId());
            if (order != null && order.getStatus() != null && order.getStatus() == 3) {
                order.setStatus(4); // 已完成
                order.setFinishTime(LocalDateTime.now());
                orderService.updateById(order);
            }
        }

        return ServerResponseEntity.success();
    }

    /**
     * 团课批量结课
     */
    @PostMapping("/group/settle/{scheduleId}")
    public ServerResponseEntity<Void> groupSettle(
            @PathVariable Long scheduleId,
            @RequestBody List<Long> absentUserIds) {
        courseCheckinService.settleGroupSchedule(scheduleId, absentUserIds);
        return ServerResponseEntity.success();
    }

    /**
     * 获取排课已报名用户列表（用于结课操作）
     */
    @GetMapping("/group/attendees/{scheduleId}")
    public ServerResponseEntity<List<Map<String, Object>>> getAttendees(@PathVariable Long scheduleId) {
        List<Order> paidOrders = orderService.list(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getScheduleId, scheduleId)
                        .in(Order::getStatus, 2, 3, 4)
        );
        List<Map<String, Object>> result = new ArrayList<>();
        for (Order order : paidOrders) {
            User user = userService.getById(order.getUserId());
            if (user != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("userId", user.getId());
                item.put("nickName", user.getNickName());
                item.put("phone", user.getPhone());
                item.put("avatarUrl", user.getAvatarUrl());
                item.put("orderId", order.getId());
                result.add(item);
            }
        }
        return ServerResponseEntity.success(result);
    }

    /**
     * 消课记录列表
     */
    @GetMapping("/list")
    public ServerResponseEntity<Page<CourseCheckin>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer checkinType,
            @RequestParam(required = false) Long packageId,
            @RequestParam(required = false) Long scheduleId) {
        Page<CourseCheckin> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CourseCheckin> wrapper = new LambdaQueryWrapper<>();
        if (checkinType != null) wrapper.eq(CourseCheckin::getCheckinType, checkinType);
        if (packageId != null) wrapper.eq(CourseCheckin::getPackageId, packageId);
        if (scheduleId != null) wrapper.eq(CourseCheckin::getScheduleId, scheduleId);
        wrapper.orderByDesc(CourseCheckin::getCheckinTime);
        return ServerResponseEntity.success(courseCheckinService.page(page, wrapper));
    }

    /**
     * 用户课包列表
     */
    @GetMapping("/package/list")
    public ServerResponseEntity<Page<UserCoursePackage>> packageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId) {
        Page<UserCoursePackage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserCoursePackage> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) wrapper.eq(UserCoursePackage::getUserId, userId);
        wrapper.orderByDesc(UserCoursePackage::getCreateTime);
        Page<UserCoursePackage> result = userCoursePackageService.page(page, wrapper);
        syncExpiredPackages(result.getRecords());
        return ServerResponseEntity.success(result);
    }

    /**
     * 标记缺勤
     */
    @PutMapping("/absent/{id}")
    public ServerResponseEntity<Void> markAbsent(@PathVariable Long id) {
        CourseCheckin checkin = courseCheckinService.getById(id);
        if (checkin == null) {
            return ServerResponseEntity.fail("记录不存在");
        }
        if (checkin.getStatus() != null && checkin.getStatus() == 0) {
            return ServerResponseEntity.success();
        }
        checkin.setStatus(0);
        courseCheckinService.updateById(checkin);

        // 如果是私教课缺勤，退还已用节数
        if (checkin.getCheckinType() == 1 && checkin.getPackageId() != null) {
            UserCoursePackage pkg = userCoursePackageService.getById(checkin.getPackageId());
            if (pkg != null) {
                LocalDateTime now = LocalDateTime.now();
                boolean wasCompleted = pkg.getStatus() != null && pkg.getStatus() == 3;
                int usedLessons = pkg.getUsedLessons() == null ? 0 : pkg.getUsedLessons();
                int totalLessons = pkg.getTotalLessons() == null ? 0 : pkg.getTotalLessons();
                pkg.setUsedLessons(Math.max(0, usedLessons - 1));
                if (pkg.getUsedLessons() < totalLessons) {
                    pkg.setStatus(isPackageExpired(pkg, now) ? 0 : 1);
                }
                userCoursePackageService.updateById(pkg);

                // 如果课包从已完成状态恢复，将订单状态也恢复为待排课
                if (wasCompleted && pkg.getStatus() != null && pkg.getStatus() == 1 && pkg.getOrderId() != null) {
                    Order order = orderService.getById(pkg.getOrderId());
                    if (order != null && order.getStatus() != null && order.getStatus() == 4) {
                        order.setStatus(3); // 恢复为待排课
                        order.setFinishTime(null);
                        orderService.updateById(order);
                    }
                }
            }
        }

        return ServerResponseEntity.success();
    }

    private void syncExpiredPackages(List<UserCoursePackage> packages) {
        if (packages == null || packages.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        List<UserCoursePackage> expiredPackages = new ArrayList<>();
        for (UserCoursePackage pkg : packages) {
            if (isExpiredActivePackage(pkg, now)) {
                pkg.setStatus(0);
                expiredPackages.add(pkg);
            }
        }
        if (!expiredPackages.isEmpty()) {
            userCoursePackageService.updateBatchById(expiredPackages);
        }
    }

    private boolean isExpiredActivePackage(UserCoursePackage pkg, LocalDateTime now) {
        return pkg != null
                && pkg.getStatus() != null
                && pkg.getStatus() == 1
                && isPackageExpired(pkg, now);
    }

    private boolean isPackageExpired(UserCoursePackage pkg, LocalDateTime now) {
        return pkg != null
                && pkg.getExpireTime() != null
                && !pkg.getExpireTime().isAfter(now);
    }
}
