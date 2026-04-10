package com.sportedu.backend.course.controller;

import com.sportedu.backend.common.api.ApiResponse;
import com.sportedu.backend.common.api.PageResponse;
import com.sportedu.backend.course.dto.CourseResponse;
import com.sportedu.backend.course.dto.CourseSaveRequest;
import com.sportedu.backend.course.dto.CourseStatusRequest;
import com.sportedu.backend.course.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/courses")
public class AdminCourseController {

    private final CourseService courseService;

    public AdminCourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ApiResponse<PageResponse<CourseResponse>> page(@RequestParam(defaultValue = "1") long pageNo,
                                                          @RequestParam(defaultValue = "10") long pageSize,
                                                          @RequestParam(required = false) Integer status,
                                                          @RequestParam(required = false) Integer courseType,
                                                          @RequestParam(required = false) String keyword) {
        return ApiResponse.success(courseService.adminPage(pageNo, pageSize, status, courseType, keyword));
    }

    @PostMapping
    public ApiResponse<CourseResponse> create(@Valid @RequestBody CourseSaveRequest request) {
        return ApiResponse.success(courseService.create(request));
    }

    @GetMapping("/{courseId}")
    public ApiResponse<CourseResponse> detail(@PathVariable Long courseId) {
        return ApiResponse.success(courseService.adminDetail(courseId));
    }

    @PutMapping("/{courseId}")
    public ApiResponse<CourseResponse> update(@PathVariable Long courseId, @Valid @RequestBody CourseSaveRequest request) {
        return ApiResponse.success(courseService.update(courseId, request));
    }

    @PostMapping("/{courseId}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long courseId, @Valid @RequestBody CourseStatusRequest request) {
        courseService.updateStatus(courseId, request.status());
        return ApiResponse.success();
    }
}
