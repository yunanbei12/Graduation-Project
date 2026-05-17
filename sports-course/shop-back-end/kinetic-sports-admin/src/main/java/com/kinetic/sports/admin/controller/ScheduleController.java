package com.kinetic.sports.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.admin.model.request.BatchScheduleCreateRequest;
import com.kinetic.sports.bean.model.BatchScheduleCreateCommand;
import com.kinetic.sports.bean.model.Coach;
import com.kinetic.sports.bean.model.Course;
import com.kinetic.sports.bean.model.CourseLocation;
import com.kinetic.sports.bean.model.CourseSchedule;
import com.kinetic.sports.bean.model.Order;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.CourseCheckinService;
import com.kinetic.sports.service.CoachService;
import com.kinetic.sports.service.CourseLocationService;
import com.kinetic.sports.service.CourseScheduleService;
import com.kinetic.sports.service.CourseService;
import com.kinetic.sports.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final CourseScheduleService courseScheduleService;
    private final CourseService courseService;
    private final CoachService coachService;
    private final CourseLocationService courseLocationService;
    private final OrderService orderService;
    private final CourseCheckinService courseCheckinService;

    @GetMapping("/list")
    public ServerResponseEntity<Page<CourseSchedule>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long locationId) {
        Page<CourseSchedule> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getType, 2);
        if (courseId != null) {
            courseWrapper.eq(Course::getId, courseId);
        }
        if (locationId != null) {
            courseWrapper.eq(Course::getLocationId, locationId);
        }
        List<Long> groupCourseIds = courseService.list(courseWrapper).stream()
                .map(Course::getId)
                .toList();
        LambdaQueryWrapper<CourseSchedule> wrapper = new LambdaQueryWrapper<>();
        if (groupCourseIds.isEmpty()) {
            Page<CourseSchedule> emptyPage = new Page<>(pageNum, pageSize);
            emptyPage.setRecords(List.of());
            return ServerResponseEntity.success(emptyPage);
        }
        wrapper.in(CourseSchedule::getCourseId, groupCourseIds);
        wrapper.orderByDesc(CourseSchedule::getScheduleDate);
        return ServerResponseEntity.success(courseScheduleService.page(page, wrapper));
    }

    @GetMapping("/board")
    public ServerResponseEntity<List<Map<String, Object>>> board(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long coachId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer status) {
        LocalDate today = LocalDate.now();
        LocalDate resolvedStart = startDate != null ? startDate : today.minusDays(today.getDayOfWeek().getValue() - 1L);
        LocalDate resolvedEnd = endDate != null ? endDate : resolvedStart.plusDays(6);
        if (resolvedEnd.isBefore(resolvedStart)) {
            return ServerResponseEntity.fail("结束日期不能早于开始日期");
        }

        List<Course> groupCourses = courseService.list(new LambdaQueryWrapper<Course>().eq(Course::getType, 2));
        if (groupCourses.isEmpty()) {
            return ServerResponseEntity.success(List.of());
        }

        Map<Long, Course> courseMap = groupCourses.stream()
                .collect(Collectors.toMap(Course::getId, item -> item, (a, b) -> a, LinkedHashMap::new));
        Set<Long> groupCourseIds = courseMap.keySet();

        LambdaQueryWrapper<CourseSchedule> wrapper = new LambdaQueryWrapper<CourseSchedule>()
                .in(CourseSchedule::getCourseId, groupCourseIds)
                .ge(CourseSchedule::getScheduleDate, resolvedStart)
                .le(CourseSchedule::getScheduleDate, resolvedEnd)
                .orderByAsc(CourseSchedule::getScheduleDate)
                .orderByAsc(CourseSchedule::getStartTime)
                .orderByAsc(CourseSchedule::getCoachId);
        if (coachId != null) {
            wrapper.eq(CourseSchedule::getCoachId, coachId);
        }
        if (status != null) {
            wrapper.eq(CourseSchedule::getStatus, status);
        }

        List<CourseSchedule> schedules = courseScheduleService.list(wrapper);
        if (schedules.isEmpty()) {
            return ServerResponseEntity.success(List.of());
        }

        Set<Long> coachIds = schedules.stream()
                .map(CourseSchedule::getCoachId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Coach> coachMap = coachIds.isEmpty() ? Map.of() : coachService.listByIds(coachIds).stream()
                .collect(Collectors.toMap(Coach::getId, item -> item, (a, b) -> a));

        List<Map<String, Object>> result = schedules.stream()
                .map(schedule -> {
                    Course course = courseMap.get(schedule.getCourseId());
                    Coach coach = schedule.getCoachId() != null ? coachMap.get(schedule.getCoachId()) : null;
                    String resolvedLocation = resolveLocation(schedule, course);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("scheduleId", schedule.getId());
                    item.put("courseId", schedule.getCourseId());
                    item.put("courseName", course != null ? course.getName() : null);
                    item.put("coachId", schedule.getCoachId());
                    item.put("coachName", coach != null ? coach.getName() : null);
                    item.put("coachAvatar", coach != null ? coach.getAvatar() : null);
                    item.put("scheduleDate", schedule.getScheduleDate());
                    item.put("startTime", resolveStartTime(schedule, course));
                    item.put("endTime", resolveEndTime(schedule, course));
                    item.put("location", resolvedLocation);
                    item.put("totalSeats", schedule.getTotalSeats());
                    item.put("enrolledSeats", schedule.getEnrolledSeats());
                    item.put("remainSeats", Math.max(0,
                            (schedule.getTotalSeats() != null ? schedule.getTotalSeats() : 0)
                                    - (schedule.getEnrolledSeats() != null ? schedule.getEnrolledSeats() : 0)));
                    item.put("status", schedule.getStatus());
                    return item;
                })
                .filter(item -> location == null || location.isBlank() || Objects.equals(location, item.get("location")))
                .toList();

        return ServerResponseEntity.success(result);
    }

    @PostMapping
    public ServerResponseEntity<Void> save(@RequestBody CourseSchedule schedule) {
        courseScheduleService.saveGroupSchedule(schedule);
        return ServerResponseEntity.success();
    }

    @PostMapping("/batch")
    public ServerResponseEntity<Integer> batchSave(@RequestBody BatchScheduleCreateRequest request) {
        BatchScheduleCreateCommand command = new BatchScheduleCreateCommand();
        command.setCourseId(request.getCourseId());
        command.setCoachId(request.getCoachId());
        command.setScheduleDates(request.getScheduleDates());
        command.setTotalSeats(request.getTotalSeats());
        return ServerResponseEntity.success(courseScheduleService.saveBatchGroupSchedules(command));
    }

    @PutMapping
    public ServerResponseEntity<Void> update(@RequestBody CourseSchedule schedule) {
        courseScheduleService.updateGroupSchedule(schedule);
        return ServerResponseEntity.success();
    }

    @DeleteMapping("/{id}")
    public ServerResponseEntity<Void> delete(@PathVariable Long id) {
        CourseSchedule schedule = courseScheduleService.getById(id);
        if (schedule == null) {
            return ServerResponseEntity.fail("排课不存在");
        }
        long relatedOrderCount = orderService.count(new LambdaQueryWrapper<Order>()
                .eq(Order::getScheduleId, id)
                .in(Order::getStatus, 1, 2, 3, 4, 6));
        if (relatedOrderCount > 0) {
            return ServerResponseEntity.fail("该排课已有报名订单，不能删除");
        }
        if (courseCheckinService.hasGroupCheckins(id)) {
            return ServerResponseEntity.fail("该排课已有结课记录，不能删除");
        }
        courseScheduleService.removeById(id);
        return ServerResponseEntity.success();
    }

    /**
     * 开课前成团判断：检查已报名人数是否达到成团人数
     * 成团：排课状态改为1（进行中）
     * 不成团：排课取消，对应待支付订单取消安排退款
     */
    @PostMapping("/check-group/{scheduleId}")
    public ServerResponseEntity<Map<String, Object>> checkGroup(@PathVariable Long scheduleId) {
        return ServerResponseEntity.success(orderService.checkGroupSchedule(scheduleId));
    }

    private String resolveLocation(CourseSchedule schedule, Course course) {
        if (course != null && course.getLocationId() != null) {
            CourseLocation location = courseLocationService.getById(course.getLocationId());
            if (location != null) {
                return location.getName();
            }
        }
        if (course != null && course.getLocation() != null && !course.getLocation().isBlank()) {
            return course.getLocation();
        }
        return schedule.getLocation();
    }

    private LocalDateTime resolveStartTime(CourseSchedule schedule, Course course) {
        if (schedule.getStartTime() != null) {
            return schedule.getStartTime();
        }
        if (schedule.getScheduleDate() != null && course != null
                && course.getStartHour() != null && !course.getStartHour().isBlank()) {
            return LocalDateTime.parse(schedule.getScheduleDate() + "T" + course.getStartHour() + ":00");
        }
        return null;
    }

    private LocalDateTime resolveEndTime(CourseSchedule schedule, Course course) {
        if (schedule.getEndTime() != null) {
            return schedule.getEndTime();
        }
        if (schedule.getScheduleDate() != null && course != null
                && course.getEndHour() != null && !course.getEndHour().isBlank()) {
            return LocalDateTime.parse(schedule.getScheduleDate() + "T" + course.getEndHour() + ":00");
        }
        return null;
    }
}
