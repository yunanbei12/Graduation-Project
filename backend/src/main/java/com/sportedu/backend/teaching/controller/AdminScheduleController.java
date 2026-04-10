package com.sportedu.backend.teaching.controller;

import com.sportedu.backend.common.api.ApiResponse;
import com.sportedu.backend.common.api.PageResponse;
import com.sportedu.backend.teaching.dto.CancelScheduleRequest;
import com.sportedu.backend.teaching.dto.ScheduleResponse;
import com.sportedu.backend.teaching.dto.ScheduleSaveRequest;
import com.sportedu.backend.teaching.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/v1/schedules")
public class AdminScheduleController {

    private final ScheduleService scheduleService;

    public AdminScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ScheduleResponse>> page(@RequestParam(defaultValue = "1") long pageNo,
                                                            @RequestParam(defaultValue = "10") long pageSize,
                                                            @RequestParam(required = false) Long courseId,
                                                            @RequestParam(required = false) Long coachId,
                                                            @RequestParam(required = false) Integer status,
                                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResponse.success(scheduleService.adminPage(pageNo, pageSize, courseId, coachId, status, startDate, endDate));
    }

    @PostMapping
    public ApiResponse<ScheduleResponse> create(@Valid @RequestBody ScheduleSaveRequest request) {
        return ApiResponse.success(scheduleService.create(request));
    }

    @PostMapping("/{scheduleId}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long scheduleId, @RequestBody(required = false) CancelScheduleRequest request) {
        scheduleService.cancel(scheduleId, request == null ? null : request.reason());
        return ApiResponse.success();
    }
}
