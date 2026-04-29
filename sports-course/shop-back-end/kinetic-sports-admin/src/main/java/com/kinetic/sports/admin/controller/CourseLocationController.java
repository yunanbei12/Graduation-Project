package com.kinetic.sports.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kinetic.sports.bean.model.Course;
import com.kinetic.sports.bean.model.CourseLocation;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.CourseLocationService;
import com.kinetic.sports.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course/location")
@RequiredArgsConstructor
public class CourseLocationController {

    private final CourseLocationService courseLocationService;
    private final CourseService courseService;

    @GetMapping("/list")
    public ServerResponseEntity<List<CourseLocation>> list(@RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<CourseLocation> wrapper = new LambdaQueryWrapper<CourseLocation>()
                .orderByAsc(CourseLocation::getSort)
                .orderByAsc(CourseLocation::getId);
        if (status != null) {
            wrapper.eq(CourseLocation::getStatus, status);
        }
        return ServerResponseEntity.success(courseLocationService.list(wrapper));
    }

    @GetMapping("/detail/{id}")
    public ServerResponseEntity<CourseLocation> detail(@PathVariable Long id) {
        return ServerResponseEntity.success(courseLocationService.getById(id));
    }

    @PostMapping
    public ServerResponseEntity<Void> save(@RequestBody CourseLocation location) {
        validateNameUnique(location);
        courseLocationService.save(location);
        return ServerResponseEntity.success();
    }

    @PutMapping
    public ServerResponseEntity<Void> update(@RequestBody CourseLocation location) {
        validateNameUnique(location);
        courseLocationService.updateById(location);
        return ServerResponseEntity.success();
    }

    @DeleteMapping("/{id}")
    public ServerResponseEntity<Void> delete(@PathVariable Long id) {
        long relatedCourseCount = courseService.count(
                new LambdaQueryWrapper<Course>().eq(Course::getLocationId, id)
        );
        if (relatedCourseCount > 0) {
            return ServerResponseEntity.fail("该地点已被团课模板使用，请先修改相关课程后再删除");
        }
        courseLocationService.removeById(id);
        return ServerResponseEntity.success();
    }

    private void validateNameUnique(CourseLocation location) {
        if (location == null || location.getName() == null || location.getName().isBlank()) {
            throw new IllegalArgumentException("请输入地点名称");
        }
        boolean exists = courseLocationService.count(
                new LambdaQueryWrapper<CourseLocation>()
                        .eq(CourseLocation::getName, location.getName())
                        .ne(location.getId() != null, CourseLocation::getId, location.getId())
        ) > 0;
        if (exists) {
            throw new IllegalArgumentException("地点名称已存在");
        }
    }
}
