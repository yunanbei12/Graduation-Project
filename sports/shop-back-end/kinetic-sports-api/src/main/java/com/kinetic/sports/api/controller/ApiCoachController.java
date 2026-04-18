package com.kinetic.sports.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.bean.model.Coach;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.CoachService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coach")
@RequiredArgsConstructor
public class ApiCoachController {

    private final CoachService coachService;

    @GetMapping("/list")
    public ServerResponseEntity<Page<Coach>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Coach> page = new Page<>(pageNum, pageSize);
        return ServerResponseEntity.success(coachService.page(page,
                new LambdaQueryWrapper<Coach>().eq(Coach::getStatus, 1).orderByDesc(Coach::getRating)));
    }

    @GetMapping("/detail/{id}")
    public ServerResponseEntity<Coach> detail(@PathVariable Long id) {
        return ServerResponseEntity.success(coachService.getById(id));
    }
}
