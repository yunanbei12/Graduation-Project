package com.kinetic.sports.service.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kinetic.sports.bean.model.Course;
import com.kinetic.sports.bean.model.CourseSchedule;
import com.kinetic.sports.service.CourseCheckinService;
import com.kinetic.sports.service.CourseScheduleService;
import com.kinetic.sports.service.CourseService;
import com.kinetic.sports.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.kinetic.sports.bean.model.Order;

/**
 * 团课排课定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CourseScheduleTask {

    private final CourseScheduleService courseScheduleService;
    private final CourseService courseService;
    private final OrderService orderService;
    private final CourseCheckinService courseCheckinService;

    /**
     * 每30分钟执行一次，检查并更新过期团课状态
     * 1. 将已过期但未成团的课程状态更新为"已取消"（自动处理）
     * 2. 已结束的课程需要管理员手动结课确认考勤（仅日志提醒）
     * 
     * initialDelay = 10000 表示应用启动10秒后立即执行一次
     * fixedDelay = 1800000 表示每次执行完成后间隔30分钟再执行
     */
    @Scheduled(initialDelay = 10000, fixedDelay = 1800000) // 启动10秒后执行，之后每30分钟执行一次
    public void updateExpiredScheduleStatus() {
        log.info("开始执行团课状态更新定时任务");
        
        try {
            // 1. 处理未成团的过期课程
            cancelUngroupedSchedules();
            
            // 2. 处理已结束的课程
            finishCompletedSchedules();
            
            log.info("团课状态更新定时任务执行完成");
        } catch (Exception e) {
            log.error("团课状态更新定时任务执行失败", e);
        }
    }

    /**
     * 取消未成团的过期课程
     */
    private void cancelUngroupedSchedules() {
        LocalDateTime now = LocalDateTime.now();
        
        log.info("开始检查未成团的过期团课，当前时间: {}", now);
        
        // 查询已到开课时间但仍未开始的团课
        List<CourseSchedule> schedules = courseScheduleService.list(
                new LambdaQueryWrapper<CourseSchedule>()
                        .eq(CourseSchedule::getStatus, 0) // 未开始
        );
        
        log.info("找到 {} 个待检查的未开始团课", schedules.size());
        
        for (CourseSchedule schedule : schedules) {
            try {
                log.info("处理团课: id={}, courseId={}, scheduleDate={}, status={}", 
                        schedule.getId(), schedule.getCourseId(), schedule.getScheduleDate(), schedule.getStatus());
                
                Course course = courseService.getById(schedule.getCourseId());
                LocalDateTime scheduleStart = resolveStartTime(schedule, course);
                // 兜底任务只处理“已经到开课时间”的场次，避免提前干预正常报名流程。
                if (scheduleStart == null || now.isBefore(scheduleStart)) {
                    continue;
                }
                int minGroupSize = (course != null && course.getMinGroupSize() != null) 
                        ? course.getMinGroupSize() : 1;
                
                long enrolledCount = orderService.count(new LambdaQueryWrapper<Order>()
                        .eq(Order::getScheduleId, schedule.getId())
                        .in(Order::getStatus, 2, 3, 4));
                
                log.info("团课 {} 报名情况: {}/{} 人", schedule.getId(), enrolledCount, minGroupSize);
                
                if (enrolledCount < minGroupSize) {
                    // 未成团时复用统一成团判断逻辑，确保取消、退款、状态更新走同一套规则。
                    orderService.checkGroupSchedule(schedule.getId());
                    log.info("✓ 团课 {} 未成团（{}/{}人），已自动取消", 
                            schedule.getId(), enrolledCount, minGroupSize);
                } else {
                    // 已达到成团人数，标记为进行中
                    schedule.setStatus(1); // 进行中
                    courseScheduleService.updateById(schedule);
                    log.info("✓ 团课 {} 已成团（{}/{}人），状态更新为进行中", 
                            schedule.getId(), enrolledCount, minGroupSize);
                }
            } catch (Exception e) {
                log.error("✗ 处理团课 {} 状态失败", schedule.getId(), e);
            }
        }
    }

    /**
     * 处理已结束但未结课的课程
     * 注意：定时任务只标记状态，不自动生成签到记录
     * 签到和考勤必须由管理员手动确认
     */
    private void finishCompletedSchedules() {
        LocalDateTime now = LocalDateTime.now();
        
        // 查询已过下课时间、仍处于进行中的团课
        List<CourseSchedule> schedules = courseScheduleService.list(
                new LambdaQueryWrapper<CourseSchedule>()
                        .eq(CourseSchedule::getStatus, 1) // 进行中
        );
        
        log.info("找到 {} 个进行中的过期团课", schedules.size());
        
        for (CourseSchedule schedule : schedules) {
            try {
                Course course = courseService.getById(schedule.getCourseId());
                LocalDateTime scheduleEnd = resolveEndTime(schedule, course);
                
                // 课程已结束，但必须有管理员手动结课确认考勤
                if (scheduleEnd != null && now.isAfter(scheduleEnd)) {
                    // 不自动生成签到记录，等待管理员手动结课
                    // 这里只记录日志提醒，不做状态变更
                    log.warn("团课 {} 已结束（结束时间: {}），请管理员及时结课并确认考勤", 
                            schedule.getId(), scheduleEnd);
                }
            } catch (Exception e) {
                log.error("处理团课 {} 结束状态失败", schedule.getId(), e);
            }
        }
    }

    /**
     * 解析课程开始时间
     */
    private LocalDateTime resolveStartTime(CourseSchedule schedule, Course course) {
        if (schedule.getStartTime() != null) {
            return schedule.getStartTime();
        }
        if (schedule.getScheduleDate() != null && course != null && course.getStartHour() != null) {
            try {
                LocalTime startTime = LocalTime.parse(course.getStartHour());
                return LocalDateTime.of(schedule.getScheduleDate(), startTime);
            } catch (Exception e) {
                log.warn("解析课程开始时间失败: {}", course.getStartHour(), e);
            }
        }
        return null;
    }

    /**
     * 解析课程结束时间
     */
    private LocalDateTime resolveEndTime(CourseSchedule schedule, Course course) {
        // 优先使用排课快照中的结束时间，避免重复解析课程模板
        if (schedule.getEndTime() != null) {
            return schedule.getEndTime();
        }
        
        // 使用课程模板的结束时间
        if (schedule.getScheduleDate() != null) {
            if (course != null && course.getEndHour() != null) {
                try {
                    // 解析时间字符串，如 "17:00"
                    LocalTime endTime = LocalTime.parse(course.getEndHour());
                    return LocalDateTime.of(schedule.getScheduleDate(), endTime);
                } catch (Exception e) {
                    log.warn("解析课程结束时间失败: {}", course.getEndHour(), e);
                }
            }
        }
        
        // 默认认为当天23:59:59结束
        if (schedule.getScheduleDate() != null) {
            return LocalDateTime.of(schedule.getScheduleDate(), LocalTime.of(23, 59, 59));
        }
        
        return null;
    }
}
