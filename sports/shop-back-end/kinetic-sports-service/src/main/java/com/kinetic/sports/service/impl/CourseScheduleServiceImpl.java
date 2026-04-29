package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kinetic.sports.bean.model.BatchScheduleCreateCommand;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.model.Coach;
import com.kinetic.sports.bean.model.Course;
import com.kinetic.sports.bean.model.CourseLocation;
import com.kinetic.sports.bean.model.CourseSchedule;
import com.kinetic.sports.service.CourseLocationService;
import com.kinetic.sports.service.CoachService;
import com.kinetic.sports.service.CourseScheduleService;
import com.kinetic.sports.service.CourseService;
import com.kinetic.sports.service.mapper.CourseScheduleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CourseScheduleServiceImpl extends ServiceImpl<CourseScheduleMapper, CourseSchedule> implements CourseScheduleService {

    private final CourseService courseService;
    private final CoachService coachService;
    private final CourseLocationService courseLocationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGroupSchedule(CourseSchedule schedule) {
        validateRequiredFields(schedule);
        Course course = getAndValidateCourse(schedule.getCourseId());
        Coach coach = getAndValidateCoach(schedule.getCoachId());
        validateScheduleConstraints(schedule, course);
        validateScheduleDuplicate(schedule);
        normalizeSchedule(schedule, course, null);
        validateCoachConflict(schedule, course, coach);
        if (schedule.getEnrolledSeats() == null) {
            schedule.setEnrolledSeats(0);
        }
        if (schedule.getStatus() == null) {
            schedule.setStatus(0);
        }
        save(schedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveBatchGroupSchedules(BatchScheduleCreateCommand command) {
        if (command == null || command.getScheduleDates() == null || command.getScheduleDates().isEmpty()) {
            throw new IllegalArgumentException("请选择至少一个排课日期");
        }

        Set<java.time.LocalDate> distinctDates = new LinkedHashSet<>(command.getScheduleDates());
        if (distinctDates.isEmpty()) {
            throw new IllegalArgumentException("请选择至少一个排课日期");
        }

        for (java.time.LocalDate scheduleDate : distinctDates) {
            CourseSchedule schedule = new CourseSchedule();
            schedule.setCourseId(command.getCourseId());
            schedule.setCoachId(command.getCoachId());
            schedule.setScheduleDate(scheduleDate);
            schedule.setTotalSeats(command.getTotalSeats());
            saveGroupSchedule(schedule);
        }
        return distinctDates.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroupSchedule(CourseSchedule schedule) {
        if (schedule.getId() == null) {
            throw new IllegalArgumentException("排课ID不能为空");
        }
        CourseSchedule existing = getById(schedule.getId());
        if (existing == null) {
            throw new IllegalArgumentException("排课不存在");
        }

        existing.setCourseId(schedule.getCourseId());
        existing.setCoachId(schedule.getCoachId());
        existing.setScheduleDate(schedule.getScheduleDate());
        existing.setTotalSeats(schedule.getTotalSeats());

        validateRequiredFields(existing);
        Course course = getAndValidateCourse(existing.getCourseId());
        Coach coach = getAndValidateCoach(existing.getCoachId());
        validateScheduleConstraints(existing, course);
        validateScheduleDuplicate(existing);
        normalizeSchedule(existing, course, schedule);
        validateCoachConflict(existing, course, coach);
        updateById(existing);
    }

    private void validateRequiredFields(CourseSchedule schedule) {
        if (schedule.getCourseId() == null) {
            throw new IllegalArgumentException("请选择团课课程");
        }
        if (schedule.getScheduleDate() == null) {
            throw new IllegalArgumentException("请选择排课日期");
        }
        if (schedule.getCoachId() == null) {
            throw new IllegalArgumentException("请选择授课教练");
        }
        if (schedule.getTotalSeats() == null || schedule.getTotalSeats() <= 0) {
            throw new IllegalArgumentException("请输入正确的总座位数");
        }
    }

    private void validateScheduleConstraints(CourseSchedule schedule, Course course) {
        if (schedule.getScheduleDate() != null && schedule.getScheduleDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("排课日期不能早于今天");
        }
        int minGroupSize = course != null && course.getMinGroupSize() != null ? course.getMinGroupSize() : 1;
        if (schedule.getTotalSeats() != null && schedule.getTotalSeats() < minGroupSize) {
            throw new IllegalArgumentException("总座位数不能少于成团人数 " + minGroupSize + " 人");
        }
    }

    private Course getAndValidateCourse(Long courseId) {
        Course course = courseService.getById(courseId);
        if (course == null || !Objects.equals(course.getType(), 2)) {
            throw new IllegalArgumentException("团课课程不存在");
        }
        if (course.getStartHour() == null || course.getStartHour().isBlank()
                || course.getEndHour() == null || course.getEndHour().isBlank()) {
            throw new IllegalArgumentException("该团课模版未配置时间段，请先完善模版");
        }
        return course;
    }

    private Coach getAndValidateCoach(Long coachId) {
        Coach coach = coachService.getById(coachId);
        if (coach == null || !Objects.equals(coach.getStatus(), 1)) {
            throw new IllegalArgumentException("授课教练不存在或已停用");
        }
        return coach;
    }

    private void normalizeSchedule(CourseSchedule schedule, Course course, CourseSchedule incoming) {
        LocalTime startHour = parseTime(course.getStartHour(), "开始时间");
        LocalTime endHour = parseTime(course.getEndHour(), "结束时间");
        if (!endHour.isAfter(startHour)) {
            throw new IllegalArgumentException("团课模版时间段配置有误，请先检查开始和结束时间");
        }

        schedule.setLocation(resolveCourseLocation(course));
        schedule.setStartTime(LocalDateTime.of(schedule.getScheduleDate(), startHour));
        schedule.setEndTime(LocalDateTime.of(schedule.getScheduleDate(), endHour));

        if (incoming != null) {
            if (incoming.getEnrolledSeats() != null) {
                schedule.setEnrolledSeats(incoming.getEnrolledSeats());
            }
            if (incoming.getStatus() != null) {
                schedule.setStatus(incoming.getStatus());
            }
        }
    }

    private void validateCoachConflict(CourseSchedule target, Course course, Coach coach) {
        LocalDateTime targetStart = resolveStartTime(target, course);
        LocalDateTime targetEnd = resolveEndTime(target, course);
        if (targetStart == null || targetEnd == null) {
            throw new IllegalArgumentException("排课时间无法解析，请检查课程模版");
        }

        List<CourseSchedule> coachSchedules = list(
                new LambdaQueryWrapper<CourseSchedule>()
                        .eq(CourseSchedule::getCoachId, target.getCoachId())
                        .ne(CourseSchedule::getStatus, 3)
                        .ne(target.getId() != null, CourseSchedule::getId, target.getId())
        );

        for (CourseSchedule item : coachSchedules) {
            Course itemCourse = courseService.getById(item.getCourseId());
            if (itemCourse == null) {
                continue;
            }
            LocalDateTime itemStart = resolveStartTime(item, itemCourse);
            LocalDateTime itemEnd = resolveEndTime(item, itemCourse);
            if (itemStart == null || itemEnd == null) {
                continue;
            }
            boolean overlap = targetStart.isBefore(itemEnd) && targetEnd.isAfter(itemStart);
            if (overlap) {
                String courseName = itemCourse.getName() != null ? itemCourse.getName() : ("课程ID:" + item.getCourseId());
                throw new IllegalArgumentException(
                        "教练「" + coach.getName() + "」在 "
                                + itemStart.toLocalDate() + " "
                                + itemStart.toLocalTime().toString().substring(0, 5)
                                + "-" + itemEnd.toLocalTime().toString().substring(0, 5)
                                + " 已有课程「" + courseName + "」，请更换教练或调整排课时间"
                );
            }
        }
    }

    private void validateScheduleDuplicate(CourseSchedule schedule) {
        long count = count(new LambdaQueryWrapper<CourseSchedule>()
                .eq(CourseSchedule::getCourseId, schedule.getCourseId())
                .eq(CourseSchedule::getScheduleDate, schedule.getScheduleDate())
                .ne(schedule.getId() != null, CourseSchedule::getId, schedule.getId())
                .ne(CourseSchedule::getStatus, 3));
        if (count > 0) {
            throw new IllegalArgumentException("该团课在 " + schedule.getScheduleDate() + " 已有排课，请勿重复创建");
        }
    }

    private LocalDateTime resolveStartTime(CourseSchedule schedule, Course course) {
        if (schedule.getScheduleDate() != null && course != null
                && course.getStartHour() != null && !course.getStartHour().isBlank()) {
            return LocalDateTime.of(schedule.getScheduleDate(), parseTime(course.getStartHour(), "开始时间"));
        }
        return schedule.getStartTime();
    }

    private LocalDateTime resolveEndTime(CourseSchedule schedule, Course course) {
        if (schedule.getScheduleDate() != null && course != null
                && course.getEndHour() != null && !course.getEndHour().isBlank()) {
            return LocalDateTime.of(schedule.getScheduleDate(), parseTime(course.getEndHour(), "结束时间"));
        }
        return schedule.getEndTime();
    }

    private LocalTime parseTime(String value, String label) {
        try {
            return LocalTime.parse(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("团课模版" + label + "格式不正确");
        }
    }

    private String resolveCourseLocation(Course course) {
        if (course == null) {
            return null;
        }
        if (course.getLocationId() != null) {
            CourseLocation location = courseLocationService.getById(course.getLocationId());
            if (location != null) {
                return location.getName();
            }
        }
        return course.getLocation();
    }
}
