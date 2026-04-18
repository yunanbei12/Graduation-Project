package com.kinetic.sports.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.bean.model.Course;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/list")
    public ServerResponseEntity<Page<Course>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long categoryId) {
        Page<Course> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (type != null) wrapper.eq(Course::getType, type);
        if (categoryId != null) wrapper.eq(Course::getCategoryId, categoryId);
        wrapper.orderByDesc(Course::getCreateTime);
        return ServerResponseEntity.success(courseService.page(page, wrapper));
    }

    @GetMapping("/detail/{id}")
    public ServerResponseEntity<Course> detail(@PathVariable Long id) {
        return ServerResponseEntity.success(courseService.getById(id));
    }

    @PostMapping
    public ServerResponseEntity<Void> save(@RequestBody Course course) {
        courseService.save(course);
        return ServerResponseEntity.success();
    }

    @PutMapping
    public ServerResponseEntity<Void> update(@RequestBody Course course) {
        courseService.updateById(course);
        return ServerResponseEntity.success();
    }

    @DeleteMapping("/{id}")
    public ServerResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.removeById(id);
        return ServerResponseEntity.success();
    }
}
