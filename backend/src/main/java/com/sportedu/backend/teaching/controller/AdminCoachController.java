package com.sportedu.backend.teaching.controller;

import com.sportedu.backend.common.api.ApiResponse;
import com.sportedu.backend.common.api.PageResponse;
import com.sportedu.backend.teaching.dto.CoachResponse;
import com.sportedu.backend.teaching.dto.CoachSaveRequest;
import com.sportedu.backend.teaching.dto.CoachWorkloadResponse;
import com.sportedu.backend.teaching.service.CoachService;
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
@RequestMapping("/api/admin/v1/coaches")
public class AdminCoachController {

    private final CoachService coachService;

    public AdminCoachController(CoachService coachService) {
        this.coachService = coachService;
    }

    @GetMapping
    public ApiResponse<PageResponse<CoachResponse>> page(@RequestParam(defaultValue = "1") long pageNo,
                                                         @RequestParam(defaultValue = "10") long pageSize,
                                                         @RequestParam(required = false) Integer status,
                                                         @RequestParam(required = false) String keyword) {
        return ApiResponse.success(coachService.page(pageNo, pageSize, status, keyword));
    }

    @PostMapping
    public ApiResponse<CoachResponse> create(@Valid @RequestBody CoachSaveRequest request) {
        return ApiResponse.success(coachService.create(request));
    }

    @PutMapping("/{coachId}")
    public ApiResponse<CoachResponse> update(@PathVariable Long coachId, @Valid @RequestBody CoachSaveRequest request) {
        return ApiResponse.success(coachService.update(coachId, request));
    }

    @GetMapping("/{coachId}/workload")
    public ApiResponse<CoachWorkloadResponse> workload(@PathVariable Long coachId) {
        return ApiResponse.success(coachService.workload(coachId));
    }
}
