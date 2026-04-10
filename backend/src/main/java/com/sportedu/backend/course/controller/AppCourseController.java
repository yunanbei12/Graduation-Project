package com.sportedu.backend.course.controller;

import com.sportedu.backend.common.api.ApiResponse;
import com.sportedu.backend.common.api.PageResponse;
import com.sportedu.backend.course.dto.CourseResponse;
import com.sportedu.backend.course.service.CourseService;
import com.sportedu.backend.teaching.dto.ScheduleResponse;
import com.sportedu.backend.teaching.service.ScheduleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/app/v1/courses")
public class AppCourseController {

    private final CourseService courseService;
    private final ScheduleService scheduleService;

    public AppCourseController(CourseService courseService, ScheduleService scheduleService) {
        this.courseService = courseService;
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public ApiResponse<PageResponse<CourseResponse>> page(@RequestParam(defaultValue = "1") long pageNo,
                                                          @RequestParam(defaultValue = "10") long pageSize,
                                                          @RequestParam(required = false) Integer courseType,
                                                          @RequestParam(required = false) String sportType) {
        return ApiResponse.success(courseService.appPage(pageNo, pageSize, courseType, sportType));
    }

    @GetMapping("/{courseId}")
    public ApiResponse<CourseResponse> detail(@PathVariable Long courseId) {
        return ApiResponse.success(courseService.appDetail(courseId));
    }

    @GetMapping("/{courseId}/schedules")
    public ApiResponse<List<ScheduleResponse>> schedules(@PathVariable Long courseId) {
        return ApiResponse.success(scheduleService.appSchedules(courseId));
    }
}
