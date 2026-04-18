package com.kinetic.sports.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.bean.model.Course;
import com.kinetic.sports.bean.model.CourseSchedule;
import com.kinetic.sports.bean.model.Order;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.CourseScheduleService;
import com.kinetic.sports.service.CourseService;
import com.kinetic.sports.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final CourseScheduleService courseScheduleService;
    private final CourseService courseService;
    private final OrderService orderService;

    @GetMapping("/list")
    public ServerResponseEntity<Page<CourseSchedule>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long courseId) {
        Page<CourseSchedule> page = new Page<>(pageNum, pageSize);
        // 只查团课（type=2）的排课
        List<Long> groupCourseIds = courseService.list(
                new LambdaQueryWrapper<Course>().eq(Course::getType, 2)
        ).stream().map(Course::getId).toList();
        LambdaQueryWrapper<CourseSchedule> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(CourseSchedule::getCourseId, courseId);
        } else {
            if (groupCourseIds.isEmpty()) {
                return ServerResponseEntity.success(new Page<>());
            }
            wrapper.in(CourseSchedule::getCourseId, groupCourseIds);
        }
        wrapper.orderByDesc(CourseSchedule::getScheduleDate);
        return ServerResponseEntity.success(courseScheduleService.page(page, wrapper));
    }

    @PostMapping
    public ServerResponseEntity<Void> save(@RequestBody CourseSchedule schedule) {
        courseScheduleService.save(schedule);
        return ServerResponseEntity.success();
    }

    @PutMapping
    public ServerResponseEntity<Void> update(@RequestBody CourseSchedule schedule) {
        courseScheduleService.updateById(schedule);
        return ServerResponseEntity.success();
    }

    @DeleteMapping("/{id}")
    public ServerResponseEntity<Void> delete(@PathVariable Long id) {
        courseScheduleService.removeById(id);
        return ServerResponseEntity.success();
    }

    /**
     * 开课前成团判断：检查已报名人数是否达到成团人数
     * 成团：排课状态改为1（进行中）
     * 不成团：排课取消，对应待支付订单取消安排退款
     */
    @PostMapping("/check-group/{scheduleId}")
    public ServerResponseEntity<Map<String, Object>> checkGroup(@PathVariable Long scheduleId) {
        return ServerResponseEntity.success(orderService.checkGroupSchedule(scheduleId));
    }
}
