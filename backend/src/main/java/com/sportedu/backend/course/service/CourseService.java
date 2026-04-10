package com.sportedu.backend.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sportedu.backend.common.api.PageResponse;
import com.sportedu.backend.common.enums.ResultCode;
import com.sportedu.backend.common.exception.BusinessException;
import com.sportedu.backend.common.util.JsonUtils;
import com.sportedu.backend.course.dto.CourseResponse;
import com.sportedu.backend.course.dto.CourseSaveRequest;
import com.sportedu.backend.course.entity.Course;
import com.sportedu.backend.course.mapper.CourseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CourseService {

    private final CourseMapper courseMapper;
    private final JsonUtils jsonUtils;

    public CourseService(CourseMapper courseMapper, JsonUtils jsonUtils) {
        this.courseMapper = courseMapper;
        this.jsonUtils = jsonUtils;
    }

    public PageResponse<CourseResponse> adminPage(long pageNo, long pageSize, Integer status, Integer courseType, String keyword) {
        Page<Course> page = courseMapper.selectPage(new Page<>(pageNo, pageSize), buildAdminQuery(status, courseType, keyword));
        return new PageResponse<>(page.getRecords().stream().map(this::toResponse).toList(), pageNo, pageSize, page.getTotal());
    }

    public CourseResponse adminDetail(Long courseId) {
        return toResponse(getCourse(courseId));
    }

    @Transactional
    public CourseResponse create(CourseSaveRequest request) {
        Course course = new Course();
        applySaveRequest(course, request);
        course.setDeleted(0);
        courseMapper.insert(course);
        return toResponse(courseMapper.selectById(course.getId()));
    }

    @Transactional
    public CourseResponse update(Long courseId, CourseSaveRequest request) {
        Course course = getCourse(courseId);
        applySaveRequest(course, request);
        courseMapper.updateById(course);
        return toResponse(courseMapper.selectById(courseId));
    }

    @Transactional
    public void updateStatus(Long courseId, Integer status) {
        Course course = getCourse(courseId);
        course.setStatus(status);
        courseMapper.updateById(course);
    }

    public PageResponse<CourseResponse> appPage(long pageNo, long pageSize, Integer courseType, String sportType) {
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<Course>()
            .eq(Course::getDeleted, 0)
            .eq(Course::getStatus, 1)
            .orderByDesc(Course::getOperationWeight)
            .orderByDesc(Course::getId);
        if (courseType != null) {
            queryWrapper.eq(Course::getCourseType, courseType);
        }
        if (StringUtils.hasText(sportType)) {
            queryWrapper.eq(Course::getSportType, sportType);
        }
        Page<Course> page = courseMapper.selectPage(new Page<>(pageNo, pageSize), queryWrapper);
        return new PageResponse<>(page.getRecords().stream().map(this::toResponse).toList(), pageNo, pageSize, page.getTotal());
    }

    public CourseResponse appDetail(Long courseId) {
        Course course = getCourse(courseId);
        if (!Integer.valueOf(1).equals(course.getStatus())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "课程不存在");
        }
        return toResponse(course);
    }

    public Course getCourse(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null || !Integer.valueOf(0).equals(course.getDeleted())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "课程不存在");
        }
        return course;
    }

    private LambdaQueryWrapper<Course> buildAdminQuery(Integer status, Integer courseType, String keyword) {
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<Course>()
            .eq(Course::getDeleted, 0)
            .orderByDesc(Course::getId);
        if (status != null) {
            queryWrapper.eq(Course::getStatus, status);
        }
        if (courseType != null) {
            queryWrapper.eq(Course::getCourseType, courseType);
        }
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                .like(Course::getCourseName, keyword)
                .or()
                .like(Course::getSportType, keyword)
                .or()
                .like(Course::getCourseCode, keyword));
        }
        return queryWrapper;
    }

    private void applySaveRequest(Course course, CourseSaveRequest request) {
        course.setCourseName(request.courseName());
        course.setCourseCode(request.courseCode());
        course.setCourseType(request.courseType());
        course.setSportType(request.sportType());
        course.setCoverUrl(request.coverUrl());
        course.setDescription(request.description());
        course.setDetailImages(jsonUtils.toJson(request.detailImages()));
        course.setPrice(request.price());
        course.setOriginalPrice(request.originalPrice());
        course.setLessonCount(request.lessonCount());
        course.setValidityDays(request.validityDays());
        course.setIsDoorToDoor(request.isDoorToDoor() == null ? 0 : request.isDoorToDoor());
        course.setServiceAreas(jsonUtils.toJson(request.serviceAreas()));
        course.setFixedScheduleDesc(request.fixedScheduleDesc());
        course.setFixedLocation(request.fixedLocation());
        course.setMaxParticipants(request.maxParticipants());
        course.setGroupSuccessCount(request.groupSuccessCount());
        course.setOperationWeight(request.operationWeight() == null ? 1 : request.operationWeight());
        course.setStatus(request.status() == null ? 1 : request.status());
    }

    private CourseResponse toResponse(Course course) {
        return new CourseResponse(
            course.getId(),
            course.getCourseName(),
            course.getCourseCode(),
            course.getCourseType(),
            course.getSportType(),
            course.getCoverUrl(),
            course.getDescription(),
            jsonUtils.parseStringList(course.getDetailImages()),
            course.getPrice(),
            course.getOriginalPrice(),
            course.getLessonCount(),
            course.getValidityDays(),
            course.getIsDoorToDoor(),
            jsonUtils.parseStringList(course.getServiceAreas()),
            course.getFixedScheduleDesc(),
            course.getFixedLocation(),
            course.getMaxParticipants(),
            course.getGroupSuccessCount(),
            course.getOperationWeight(),
            course.getStatus()
        );
    }
}
