package com.kinetic.sports.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.bean.model.Course;
import com.kinetic.sports.bean.model.CourseLocation;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.common.util.ContentSecurityUtils;
import com.kinetic.sports.service.CourseLocationService;
import com.kinetic.sports.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final CourseLocationService courseLocationService;

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
        Page<Course> result = courseService.page(page, wrapper);
        if (result.getRecords() != null) {
            result.setRecords(result.getRecords().stream().map(this::fillCourseLocation).toList());
        }
        return ServerResponseEntity.success(result);
    }

    @GetMapping("/detail/{id}")
    public ServerResponseEntity<Course> detail(@PathVariable Long id) {
        return ServerResponseEntity.success(fillCourseLocation(courseService.getById(id)));
    }

    @PostMapping
    public ServerResponseEntity<Void> save(@RequestBody Course course) {
        sanitizeCourseContent(course);
        normalizeCourseLocation(course);
        courseService.save(course);
        return ServerResponseEntity.success();
    }

    @PutMapping
    public ServerResponseEntity<Void> update(@RequestBody Course course) {
        sanitizeCourseContent(course);
        normalizeCourseLocation(course);
        courseService.updateById(course);
        return ServerResponseEntity.success();
    }

    @DeleteMapping("/{id}")
    public ServerResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.removeById(id);
        return ServerResponseEntity.success();
    }

    private void normalizeCourseLocation(Course course) {
        if (course == null) {
            return;
        }
        if (course.getType() == null || course.getType() != 2) {
            course.setLocationId(null);
            course.setLocation(null);
            course.setLocationImage(null);
            return;
        }
        if (course.getLocationId() == null) {
            throw new IllegalArgumentException("请选择上课地点");
        }
        CourseLocation location = courseLocationService.getById(course.getLocationId());
        if (location == null || location.getStatus() == null || location.getStatus() != 1) {
            throw new IllegalArgumentException("上课地点不存在或已停用");
        }
        course.setLocation(location.getName());
        course.setLocationImage(location.getCoverImage());
    }

    private void sanitizeCourseContent(Course course) {
        if (course == null) {
            return;
        }
        course.setName(ContentSecurityUtils.normalizeText(course.getName()));
        course.setDescription(ContentSecurityUtils.normalizeText(course.getDescription()));
        course.setDetail(ContentSecurityUtils.sanitizeRichText(course.getDetail()));
    }

    private Course fillCourseLocation(Course course) {
        if (course == null) {
            return null;
        }
        if (course.getLocationId() != null) {
            CourseLocation location = courseLocationService.getById(course.getLocationId());
            if (location != null) {
                course.setLocation(location.getName());
                course.setLocationImage(location.getCoverImage());
            }
            return course;
        }
        if (course.getLocation() != null && !course.getLocation().isBlank()) {
            CourseLocation location = courseLocationService.getOne(
                    new LambdaQueryWrapper<CourseLocation>()
                            .eq(CourseLocation::getName, course.getLocation())
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
