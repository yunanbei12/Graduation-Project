package com.kinetic.sports.service.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kinetic.sports.bean.model.Course;
import com.kinetic.sports.bean.model.CourseSchedule;
import com.kinetic.sports.bean.model.Order;
import com.kinetic.sports.service.CourseScheduleService;
import com.kinetic.sports.service.CourseService;
import com.kinetic.sports.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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

    /**
     * 每30分钟执行一次，检查并更新过期团课状态
     * 1. 将已过期但未成团的课程状态更新为"已取消"
     * 2. 将已结束的课程状态更新为"已结束"
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
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        log.info("开始检查未成团的过期团课，今天是: {}", today);
        
        // 查询昨天及之前的、状态为"未开始"的团课
        List<CourseSchedule> schedules = courseScheduleService.list(
                new LambdaQueryWrapper<CourseSchedule>()
                        .eq(CourseSchedule::getStatus, 0) // 未开始
                        .le(CourseSchedule::getScheduleDate, yesterday)
        );
        
        log.info("找到 {} 个未开始的过期团课（日期 <= {}）", schedules.size(), yesterday);
        
        for (CourseSchedule schedule : schedules) {
            try {
                log.info("处理团课: id={}, courseId={}, scheduleDate={}, status={}", 
                        schedule.getId(), schedule.getCourseId(), schedule.getScheduleDate(), schedule.getStatus());
                
                // 检查是否有报名
                Course course = courseService.getById(schedule.getCourseId());
                int minGroupSize = (course != null && course.getMinGroupSize() != null) 
                        ? course.getMinGroupSize() : 1;
                
                long enrolledCount = orderService.count(new LambdaQueryWrapper<Order>()
                        .eq(Order::getScheduleId, schedule.getId())
                        .in(Order::getStatus, 1, 2, 3)); // 已支付、进行中、已完成
                
                log.info("团课 {} 报名情况: {}/{} 人", schedule.getId(), enrolledCount, minGroupSize);
                
                if (enrolledCount < minGroupSize) {
                    // 未达到成团人数，取消课程
                    schedule.setStatus(3); // 已取消
                    courseScheduleService.updateById(schedule);
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
     * 标记已结束的课程
     */
    private void finishCompletedSchedules() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        
        // 查询昨天及之前的、状态为"进行中"的团课
        List<CourseSchedule> schedules = courseScheduleService.list(
                new LambdaQueryWrapper<CourseSchedule>()
                        .eq(CourseSchedule::getStatus, 1) // 进行中
                        .le(CourseSchedule::getScheduleDate, yesterday)
        );
        
        log.info("找到 {} 个进行中的过期团课", schedules.size());
        
        for (CourseSchedule schedule : schedules) {
            try {
                // 检查课程是否真的已经结束
                LocalDateTime scheduleEnd = resolveEndTime(schedule);
                
                if (scheduleEnd != null && LocalDateTime.now().isAfter(scheduleEnd)) {
                    schedule.setStatus(2); // 已结束
                    courseScheduleService.updateById(schedule);
                    log.info("团课 {} 已结束，状态已更新", schedule.getId());
                }
            } catch (Exception e) {
                log.error("处理团课 {} 结束状态失败", schedule.getId(), e);
            }
        }
    }

    /**
     * 解析课程结束时间
     */
    private LocalDateTime resolveEndTime(CourseSchedule schedule) {
        // 优先使用排课表中的结束时间（兼容旧数据）
        if (schedule.getEndTime() != null) {
            return schedule.getEndTime();
        }
        
        // 使用课程模板的结束时间
        if (schedule.getScheduleDate() != null) {
            Course course = courseService.getById(schedule.getCourseId());
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
