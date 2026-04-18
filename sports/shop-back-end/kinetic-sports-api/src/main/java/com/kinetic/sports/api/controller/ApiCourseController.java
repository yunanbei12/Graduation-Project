package com.kinetic.sports.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.bean.model.Course;
import com.kinetic.sports.bean.model.CourseCategory;
import com.kinetic.sports.bean.model.CourseSchedule;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.CourseService;
import com.kinetic.sports.service.CourseCategoryService;
import com.kinetic.sports.service.CourseScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
            @RequestParam(required = false) Long categoryId) {
        Page<Course> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, 1);
        if (type != null) wrapper.eq(Course::getType, type);
        if (categoryId != null) wrapper.eq(Course::getCategoryId, categoryId);
        wrapper.orderByDesc(Course::getSales);
        return ServerResponseEntity.success(courseService.page(page, wrapper));
    }

    @GetMapping("/detail/{id}")
    public ServerResponseEntity<Course> detail(@PathVariable Long id) {
        return ServerResponseEntity.success(courseService.getById(id));
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
        Page<CourseSchedule> page = new Page<>(pageNum, pageSize);
        return ServerResponseEntity.success(courseScheduleService.page(page,
                new LambdaQueryWrapper<CourseSchedule>()
                        .eq(CourseSchedule::getCourseId, courseId)
                        .in(CourseSchedule::getStatus, 0, 1) // 0=未开始可预约 1=进行中
                        .gt(CourseSchedule::getTotalSeats, 0)
                        .orderByAsc(CourseSchedule::getScheduleDate)));
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

        // 查询近 7 天内可预约的排课（状态 0=未开始）
        List<CourseSchedule> schedules = courseScheduleService.list(
                new LambdaQueryWrapper<CourseSchedule>()
                        .eq(CourseSchedule::getStatus, 0)
                        .ge(CourseSchedule::getScheduleDate, today)
                        .le(CourseSchedule::getScheduleDate, endDate)
                        .orderByAsc(CourseSchedule::getScheduleDate)
                        .last("LIMIT " + limit)
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (CourseSchedule schedule : schedules) {
            Course course = courseService.getById(schedule.getCourseId());
            if (course == null || course.getStatus() != 1 || course.getType() != 2) continue;

            Map<String, Object> item = new HashMap<>();
            item.put("scheduleId", schedule.getId());
            item.put("courseId", course.getId());
            item.put("name", course.getName());
            item.put("pic", course.getPic());
            item.put("price", course.getPrice());
            item.put("location", course.getLocation() != null ? course.getLocation() : schedule.getLocation());
            item.put("startHour", course.getStartHour());
            item.put("endHour", course.getEndHour());
            item.put("scheduleDate", schedule.getScheduleDate());
            item.put("totalSeats", schedule.getTotalSeats());
            item.put("enrolledSeats", schedule.getEnrolledSeats());
            item.put("remainSeats", schedule.getTotalSeats() - schedule.getEnrolledSeats());
            result.add(item);
        }
        return ServerResponseEntity.success(result);
    }
}
