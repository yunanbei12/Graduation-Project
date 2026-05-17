package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.model.Course;
import com.kinetic.sports.bean.model.CourseCheckin;
import com.kinetic.sports.bean.model.CourseSchedule;
import com.kinetic.sports.bean.model.Order;
import com.kinetic.sports.service.CourseCheckinService;
import com.kinetic.sports.service.mapper.CourseMapper;
import com.kinetic.sports.service.mapper.CourseCheckinMapper;
import com.kinetic.sports.service.mapper.CourseScheduleMapper;
import com.kinetic.sports.service.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CourseCheckinServiceImpl extends ServiceImpl<CourseCheckinMapper, CourseCheckin> implements CourseCheckinService {

    private final CourseScheduleMapper courseScheduleMapper;
    private final CourseMapper courseMapper;
    private final OrderMapper orderMapper;

    @Override
    public long countDistinctUsersByCoachId(Long coachId) {
        if (coachId == null) {
            return 0L;
        }
        return baseMapper.countDistinctUsersByCoachId(coachId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settleGroupSchedule(Long scheduleId, List<Long> absentUserIds) {
        CourseSchedule schedule = courseScheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new IllegalArgumentException("排课不存在");
        }
        if (Objects.equals(schedule.getStatus(), 3)) {
            throw new IllegalArgumentException("该排课已取消，不能结课");
        }

        Course course = courseMapper.selectById(schedule.getCourseId());
        if (course == null) {
            throw new IllegalArgumentException("课程不存在");
        }

        LocalDateTime scheduleEnd = resolveEndTime(schedule, course);
        // 团课必须在真正下课后才能结课，避免后台提前生成签到和完成订单。
        if (scheduleEnd != null && LocalDateTime.now().isBefore(scheduleEnd)) {
            throw new IllegalArgumentException("课程未结束，暂不能结课");
        }
        if (hasGroupCheckins(scheduleId)) {
            throw new IllegalArgumentException("该排课已结课");
        }

        BigDecimal coachRatio = course.getSettleRatio() != null ? course.getSettleRatio() : BigDecimal.ZERO;
        Set<Long> absentUserIdSet = absentUserIds == null ? Set.of() : new HashSet<>(absentUserIds);
        LocalDateTime finishTime = LocalDateTime.now();

        List<Order> paidOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getScheduleId, scheduleId)
                .in(Order::getStatus, 2, 3, 4));

        List<CourseCheckin> checkins = new ArrayList<>();
        for (Order order : paidOrders) {
            // 团课结课的本质是：按场次为每个报名学员生成签到，并同步收口订单状态。
            Long userId = order.getUserId();
            boolean isAbsent = absentUserIdSet.contains(userId);

            CourseCheckin checkin = new CourseCheckin();
            checkin.setUserId(userId);
            checkin.setCourseId(schedule.getCourseId());
            checkin.setScheduleId(scheduleId);
            checkin.setCoachId(schedule.getCoachId());
            checkin.setLocation(schedule.getLocation());
            checkin.setCheckinTime(finishTime);
            checkin.setCheckinType(2);
            checkin.setStatus(isAbsent ? 0 : 1);
            checkin.setLessonPrice(defaultPrice(course.getPrice()));
            checkin.setCoachRatio(coachRatio);
            checkin.setCoachAmount(defaultPrice(course.getPrice()).multiply(coachRatio).setScale(2, RoundingMode.HALF_UP));
            checkin.setSettleStatus(0);
            checkins.add(checkin);

            if (!Objects.equals(order.getStatus(), 4)) {
                order.setStatus(4);
                order.setFinishTime(finishTime);
                orderMapper.updateById(order);
            }
        }

        if (!checkins.isEmpty()) {
            saveBatch(checkins);
        }

        // 只有签到和订单都处理完成后，才把排课状态收口为已结课。
        if (!Objects.equals(schedule.getStatus(), 2)) {
            schedule.setStatus(2);
            courseScheduleMapper.updateById(schedule);
        }
    }

    @Override
    public boolean hasGroupCheckins(Long scheduleId) {
        if (scheduleId == null) {
            return false;
        }
        return count(new LambdaQueryWrapper<CourseCheckin>()
                .eq(CourseCheckin::getScheduleId, scheduleId)
                .eq(CourseCheckin::getCheckinType, 2)) > 0;
    }

    private LocalDateTime resolveEndTime(CourseSchedule schedule, Course course) {
        if (schedule == null) {
            return null;
        }
        if (schedule.getEndTime() != null) {
            return schedule.getEndTime();
        }
        if (schedule.getScheduleDate() != null && course != null && course.getEndHour() != null && !course.getEndHour().isBlank()) {
            try {
                return LocalDateTime.of(schedule.getScheduleDate(), LocalTime.parse(course.getEndHour()));
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private BigDecimal defaultPrice(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : amount.setScale(2, RoundingMode.HALF_UP);
    }
}
