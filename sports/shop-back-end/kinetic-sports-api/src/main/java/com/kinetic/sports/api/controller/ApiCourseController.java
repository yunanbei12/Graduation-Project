package com.kinetic.sports.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.bean.model.Course;
import com.kinetic.sports.bean.model.CourseCategory;
import com.kinetic.sports.bean.model.CourseLocation;
import com.kinetic.sports.bean.model.CourseSchedule;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.CourseService;
import com.kinetic.sports.service.CourseCategoryService;
import com.kinetic.sports.service.CourseLocationService;
import com.kinetic.sports.service.CourseScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class ApiCourseController {

    private final CourseService courseService;
    private final CourseCategoryService courseCategoryService;
    private final CourseLocationService courseLocationService;
    private final CourseScheduleService courseScheduleService;

    @GetMapping("/category/list")
    public ServerResponseEntity<java.util.List<CourseCategory>> categoryList() {
        return ServerResponseEntity.success(courseCategoryService.list(
                new LambdaQueryWrapper<CourseCategory>()
                        .eq(CourseCategory::getStatus, 1)
                        .orderByAsc(CourseCategory::getSort)
        ));
    }

    @GetMapping("/list")
    public ServerResponseEntity<Page<Course>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String location) {
        Page<Course> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, 1);
        if (type != null) wrapper.eq(Course::getType, type);
        if (categoryId != null) wrapper.eq(Course::getCategoryId, categoryId);
        if (location != null && !location.isBlank()) wrapper.eq(Course::getLocation, location);
        wrapper.orderByDesc(Course::getSales);
        Page<Course> result = courseService.page(page, wrapper);

        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            result.setRecords(result.getRecords().stream().map(this::fillCourseLocation).toList());
        }
        return ServerResponseEntity.success(result);
    }

    @GetMapping("/location/list")
    public ServerResponseEntity<List<String>> locationList(
            @RequestParam(required = false) Long categoryId) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, 1)
                .eq(Course::getType, 2)
                .orderByAsc(Course::getLocation);
        if (categoryId != null) {
            wrapper.eq(Course::getCategoryId, categoryId);
        }

        List<String> locations = courseService.list(wrapper).stream()
                .map(this::fillCourseLocation)
                .map(Course::getLocation)
                .filter(location -> location != null && !location.isBlank())
                .distinct()
                .toList();
        return ServerResponseEntity.success(locations);
    }

    @GetMapping("/location/options")
    public ServerResponseEntity<List<Map<String, Object>>> locationOptions(
            @RequestParam(required = false) Long categoryId) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, 1)
                .eq(Course::getType, 2)
                .orderByAsc(Course::getLocation);
        if (categoryId != null) {
            wrapper.eq(Course::getCategoryId, categoryId);
        }

        Map<String, Map<String, Object>> locationMap = new java.util.LinkedHashMap<>();
        for (Course course : courseService.list(wrapper)) {
            Course normalized = fillCourseLocation(course);
            if (normalized == null || normalized.getLocation() == null || normalized.getLocation().isBlank()) {
                continue;
            }
            String key = normalized.getLocationId() != null
                    ? "id:" + normalized.getLocationId()
                    : "name:" + normalized.getLocation();
            Map<String, Object> item = locationMap.computeIfAbsent(key, k -> {
                Map<String, Object> locationItem = new HashMap<>();
                locationItem.put("id", normalized.getLocationId());
                locationItem.put("name", normalized.getLocation());
                locationItem.put("coverImage", normalized.getLocationImage());
                locationItem.put("address", null);
                locationItem.put("description", null);
                locationItem.put("courseCount", 0);
                if (normalized.getLocationId() != null) {
                    CourseLocation location = courseLocationService.getById(normalized.getLocationId());
                    if (location != null) {
                        locationItem.put("address", location.getAddress());
                        locationItem.put("description", location.getDescription());
                        if (location.getCoverImage() != null && !location.getCoverImage().isBlank()) {
                            locationItem.put("coverImage", location.getCoverImage());
                        }
                    }
                }
                return locationItem;
            });
            item.put("courseCount", ((Integer) item.get("courseCount")) + 1);
        }
        return ServerResponseEntity.success(new ArrayList<>(locationMap.values()));
    }

    @GetMapping("/detail/{id}")
    public ServerResponseEntity<Course> detail(@PathVariable Long id) {
        return ServerResponseEntity.success(fillCourseLocation(courseService.getById(id)));
    }

    @GetMapping("/schedule/detail/{id}")
    public ServerResponseEntity<CourseSchedule> scheduleDetail(@PathVariable Long id) {
        return ServerResponseEntity.success(courseScheduleService.getById(id));
    }

    @GetMapping("/schedule/list")
    public ServerResponseEntity<Page<CourseSchedule>> scheduleList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam Long courseId) {
        Course course = courseService.getById(courseId);
        LocalDateTime now = LocalDateTime.now();
        List<CourseSchedule> allSchedules = courseScheduleService.list(
                new LambdaQueryWrapper<CourseSchedule>()
                        .eq(CourseSchedule::getCourseId, courseId)
                        .in(CourseSchedule::getStatus, 0, 1)
                        .apply("IFNULL(enrolled_seats, 0) < total_seats")
                        .orderByAsc(CourseSchedule::getScheduleDate)
                        .orderByAsc(CourseSchedule::getStartTime));

        List<CourseSchedule> filtered = allSchedules.stream()
                .filter(schedule -> {
                    LocalDateTime start = resolveScheduleStart(schedule, course);
                    return start != null && start.isAfter(now);
                })
                .toList();

        Page<CourseSchedule> result = new Page<>(pageNum, pageSize, filtered.size());
        int fromIndex = Math.max((pageNum - 1) * pageSize, 0);
        int toIndex = Math.min(fromIndex + pageSize, filtered.size());
        result.setRecords(fromIndex >= filtered.size() ? List.of() : filtered.subList(fromIndex, toIndex));
        return ServerResponseEntity.success(result);
    }

    /**
     * 首页近期团课排课列表
     * 返回近 7 天内可预约的团课排课，附带课程信息，按日期升序
     */
    @GetMapping("/schedule/upcoming")
    public ServerResponseEntity<List<Map<String, Object>>> upcomingSchedules(
            @RequestParam(defaultValue = "4") Integer limit) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(7);
        LocalDateTime now = LocalDateTime.now();

        List<CourseSchedule> schedules = courseScheduleService.list(
                new LambdaQueryWrapper<CourseSchedule>()
                        .in(CourseSchedule::getStatus, 0, 1)
                        .ge(CourseSchedule::getScheduleDate, today)
                        .le(CourseSchedule::getScheduleDate, endDate)
                        .apply("IFNULL(enrolled_seats, 0) < total_seats")
                        .orderByAsc(CourseSchedule::getScheduleDate)
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (CourseSchedule schedule : schedules) {
            Course course = courseService.getById(schedule.getCourseId());
            if (course == null || course.getStatus() != 1 || course.getType() != 2) continue;
            LocalDateTime scheduleStart = resolveScheduleStart(schedule, course);
            if (scheduleStart == null || !scheduleStart.isAfter(now)) continue;
            course = fillCourseLocation(course);

            Map<String, Object> item = new HashMap<>();
            item.put("scheduleId", schedule.getId());
            item.put("courseId", course.getId());
            item.put("name", course.getName());
            item.put("pic", course.getPic());
            item.put("locationImage", course.getLocationImage());
            item.put("price", course.getPrice());
            item.put("location", course.getLocation() != null ? course.getLocation() : schedule.getLocation());
            item.put("startHour", course.getStartHour());
            item.put("endHour", course.getEndHour());
            item.put("scheduleDate", schedule.getScheduleDate());
            item.put("totalSeats", schedule.getTotalSeats());
            item.put("enrolledSeats", schedule.getEnrolledSeats());
            item.put("remainSeats", schedule.getTotalSeats() - schedule.getEnrolledSeats());
            result.add(item);
            if (result.size() >= limit) {
                break;
            }
        }
        return ServerResponseEntity.success(result);
    }

    private LocalDateTime resolveScheduleStart(CourseSchedule schedule, Course course) {
        if (schedule == null) return null;
        if (schedule.getScheduleDate() != null && course != null && course.getStartHour() != null && !course.getStartHour().isEmpty()) {
            try {
                return LocalDateTime.of(schedule.getScheduleDate(), LocalTime.parse(course.getStartHour()));
            } catch (Exception ignored) {
            }
        }
        return schedule.getStartTime();
    }

    private Course fillCourseLocation(Course course) {
        if (course == null) return null;
        if (course.getLocationId() != null) {
            CourseLocation location = courseLocationService.getById(course.getLocationId());
            if (location != null && (location.getStatus() == null || location.getStatus() == 1)) {
                course.setLocation(location.getName());
                course.setLocationImage(location.getCoverImage());
                return course;
            }
        }
        if (course.getLocation() != null && !course.getLocation().isBlank()) {
            CourseLocation location = courseLocationService.getOne(
                    new LambdaQueryWrapper<CourseLocation>()
                            .eq(CourseLocation::getName, course.getLocation())
                            .eq(CourseLocation::getStatus, 1)
                            .last("limit 1")
            );
            if (location != null) {
                course.setLocationId(location.getId());
                course.setLocationImage(location.getCoverImage());
            }
            return course;
        }

        CourseSchedule fallback = courseScheduleService.getOne(
                new LambdaQueryWrapper<CourseSchedule>()
                        .eq(CourseSchedule::getCourseId, course.getId())
                        .isNotNull(CourseSchedule::getLocation)
                        .orderByAsc(CourseSchedule::getScheduleDate)
                        .orderByAsc(CourseSchedule::getStartTime)
                        .last("limit 1")
        );
        if (fallback != null && fallback.getLocation() != null && !fallback.getLocation().isBlank()) {
            course.setLocation(fallback.getLocation());
            CourseLocation location = courseLocationService.getOne(
                    new LambdaQueryWrapper<CourseLocation>()
                            .eq(CourseLocation::getName, fallback.getLocation())
                            .eq(CourseLocation::getStatus, 1)
                            .last("limit 1")
            );
            if (location != null) {
                course.setLocationId(location.getId());
                course.setLocationImage(location.getCoverImage());
            }
        }
        return course;
    }
}
