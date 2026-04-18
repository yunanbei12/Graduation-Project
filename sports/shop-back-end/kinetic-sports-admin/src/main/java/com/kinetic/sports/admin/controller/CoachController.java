package com.kinetic.sports.admin.controller;

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
public class CoachController {

    private final CoachService coachService;

    @GetMapping("/list")
    public ServerResponseEntity<Page<Coach>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) Integer status) {
        Page<Coach> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Coach> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Coach::getName, name);
        }
        if (phone != null && !phone.isEmpty()) {
            wrapper.like(Coach::getPhone, phone);
        }
        if (skills != null && !skills.isEmpty()) {
            wrapper.like(Coach::getSkills, skills);
        }
        if (status != null) {
            wrapper.eq(Coach::getStatus, status);
        }
        wrapper.orderByAsc(Coach::getId);
        return ServerResponseEntity.success(coachService.page(page, wrapper));
    }

    @GetMapping("/detail/{id}")
    public ServerResponseEntity<Coach> detail(@PathVariable Long id) {
        return ServerResponseEntity.success(coachService.getById(id));
    }

    @PostMapping
    public ServerResponseEntity<Void> save(@RequestBody Coach coach) {
        coachService.save(coach);
        return ServerResponseEntity.success();
    }

    @PutMapping
    public ServerResponseEntity<Void> update(@RequestBody Coach coach) {
        coachService.updateById(coach);
        return ServerResponseEntity.success();
    }

    @DeleteMapping("/{id}")
    public ServerResponseEntity<Void> delete(@PathVariable Long id) {
        coachService.removeById(id);
        return ServerResponseEntity.success();
    }
}
