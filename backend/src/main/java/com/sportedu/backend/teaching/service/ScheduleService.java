package com.sportedu.backend.teaching.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sportedu.backend.common.api.PageResponse;
import com.sportedu.backend.common.enums.ResultCode;
import com.sportedu.backend.common.exception.BusinessException;
import com.sportedu.backend.course.entity.Course;
import com.sportedu.backend.course.service.CourseService;
import com.sportedu.backend.teaching.dto.ScheduleResponse;
import com.sportedu.backend.teaching.dto.ScheduleSaveRequest;
import com.sportedu.backend.teaching.entity.Coach;
import com.sportedu.backend.teaching.entity.CourseSchedule;
import com.sportedu.backend.teaching.mapper.CourseScheduleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private final CourseScheduleMapper courseScheduleMapper;
    private final CourseService courseService;
    private final CoachService coachService;

    public ScheduleService(CourseScheduleMapper courseScheduleMapper,
                           CourseService courseService,
                           CoachService coachService) {
        this.courseScheduleMapper = courseScheduleMapper;
        this.courseService = courseService;
        this.coachService = coachService;
    }

    public PageResponse<ScheduleResponse> adminPage(long pageNo,
                                                    long pageSize,
                                                    Long courseId,
                                                    Long coachId,
                                                    Integer status,
                                                    LocalDate startDate,
                                                    LocalDate endDate) {
        LambdaQueryWrapper<CourseSchedule> queryWrapper = new LambdaQueryWrapper<CourseSchedule>()
            .eq(CourseSchedule::getDeleted, 0)
            .orderByAsc(CourseSchedule::getStartTime);
        if (courseId != null) {
            queryWrapper.eq(CourseSchedule::getCourseId, courseId);
        }
        if (coachId != null) {
            queryWrapper.eq(CourseSchedule::getCoachId, coachId);
        }
        if (status != null) {
            queryWrapper.eq(CourseSchedule::getStatus, status);
        }
        if (startDate != null) {
            queryWrapper.ge(CourseSchedule::getScheduleDate, startDate);
        }
        if (endDate != null) {
            queryWrapper.le(CourseSchedule::getScheduleDate, endDate);
        }
        Page<CourseSchedule> page = courseScheduleMapper.selectPage(new Page<>(pageNo, pageSize), queryWrapper);
        return new PageResponse<>(toResponses(page.getRecords()), pageNo, pageSize, page.getTotal());
    }

    @Transactional
    public ScheduleResponse create(ScheduleSaveRequest request) {
        validateScheduleRelation(request.courseId(), request.coachId());
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "结束时间必须晚于开始时间");
        }
        CourseSchedule schedule = new CourseSchedule();
        schedule.setCourseId(request.courseId());
        schedule.setCoachId(request.coachId());
        schedule.setScheduleDate(request.scheduleDate());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        schedule.setLocation(request.location());
        schedule.setCapacity(request.capacity());
        schedule.setMinGroupCount(request.minGroupCount());
        schedule.setEnrolledCount(0);
        schedule.setWaitlistCount(0);
        schedule.setStatus(0);
        schedule.setDeleted(0);
        courseScheduleMapper.insert(schedule);
        return toResponses(List.of(courseScheduleMapper.selectById(schedule.getId()))).get(0);
    }

    @Transactional
    public void cancel(Long scheduleId, String reason) {
        CourseSchedule schedule = getSchedule(scheduleId);
        schedule.setStatus(2);
        schedule.setCancelReason(reason);
        courseScheduleMapper.updateById(schedule);
    }

    public List<ScheduleResponse> appSchedules(Long courseId) {
        courseService.appDetail(courseId);
        return toResponses(courseScheduleMapper.selectList(new LambdaQueryWrapper<CourseSchedule>()
            .eq(CourseSchedule::getCourseId, courseId)
            .eq(CourseSchedule::getDeleted, 0)
            .ne(CourseSchedule::getStatus, 2)
            .orderByAsc(CourseSchedule::getStartTime)));
    }

    private CourseSchedule getSchedule(Long scheduleId) {
        CourseSchedule schedule = courseScheduleMapper.selectById(scheduleId);
        if (schedule == null || !Integer.valueOf(0).equals(schedule.getDeleted())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "排期不存在");
        }
        return schedule;
    }

    private void validateScheduleRelation(Long courseId, Long coachId) {
        courseService.getCourse(courseId);
        coachService.getCoach(coachId);
    }

    private List<ScheduleResponse> toResponses(List<CourseSchedule> schedules) {
        if (schedules.isEmpty()) {
            return List.of();
        }
        Map<Long, Course> courseMap = schedules.stream()
            .map(CourseSchedule::getCourseId)
            .distinct()
            .map(courseService::getCourse)
            .collect(Collectors.toMap(Course::getId, Function.identity()));
        Map<Long, Coach> coachMap = schedules.stream()
            .map(CourseSchedule::getCoachId)
            .distinct()
            .map(coachService::getCoach)
            .collect(Collectors.toMap(Coach::getId, Function.identity()));
        return schedules.stream().map(schedule -> {
            Course course = courseMap.get(schedule.getCourseId());
            Coach coach = coachMap.get(schedule.getCoachId());
            return new ScheduleResponse(
                schedule.getId(),
                schedule.getCourseId(),
                course == null ? null : course.getCourseName(),
                schedule.getCoachId(),
                coach == null ? null : coach.getCoachName(),
                schedule.getScheduleDate(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getLocation(),
                schedule.getCapacity(),
                schedule.getMinGroupCount(),
                schedule.getEnrolledCount(),
                schedule.getWaitlistCount(),
                schedule.getStatus(),
                schedule.getCancelReason()
            );
        }).toList();
    }
}
