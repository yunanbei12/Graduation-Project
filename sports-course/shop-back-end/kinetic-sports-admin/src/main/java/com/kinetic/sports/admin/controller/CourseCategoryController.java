package com.kinetic.sports.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kinetic.sports.bean.model.CourseCategory;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.CourseCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course/category")
@RequiredArgsConstructor
public class CourseCategoryController {

    private final CourseCategoryService courseCategoryService;

    @GetMapping("/list")
    public ServerResponseEntity<List<CourseCategory>> list() {
        return ServerResponseEntity.success(courseCategoryService.list(
                new LambdaQueryWrapper<CourseCategory>().orderByAsc(CourseCategory::getSort)
        ));
    }

    @GetMapping("/detail/{id}")
    public ServerResponseEntity<CourseCategory> detail(@PathVariable Long id) {
        return ServerResponseEntity.success(courseCategoryService.getById(id));
    }

    @PostMapping
    public ServerResponseEntity<Void> save(@RequestBody CourseCategory category) {
        courseCategoryService.save(category);
        return ServerResponseEntity.success();
    }

    @PutMapping
    public ServerResponseEntity<Void> update(@RequestBody CourseCategory category) {
        courseCategoryService.updateById(category);
        return ServerResponseEntity.success();
    }

    @DeleteMapping("/{id}")
    public ServerResponseEntity<Void> delete(@PathVariable Long id) {
        courseCategoryService.removeById(id);
        return ServerResponseEntity.success();
    }
}
