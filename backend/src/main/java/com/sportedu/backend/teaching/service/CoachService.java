package com.sportedu.backend.teaching.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sportedu.backend.common.api.PageResponse;
import com.sportedu.backend.common.enums.ResultCode;
import com.sportedu.backend.common.exception.BusinessException;
import com.sportedu.backend.common.util.JsonUtils;
import com.sportedu.backend.teaching.dto.CoachResponse;
import com.sportedu.backend.teaching.dto.CoachSaveRequest;
import com.sportedu.backend.teaching.dto.CoachWorkloadResponse;
import com.sportedu.backend.teaching.entity.Coach;
import com.sportedu.backend.teaching.entity.CourseSchedule;
import com.sportedu.backend.teaching.mapper.CoachMapper;
import com.sportedu.backend.teaching.mapper.CourseScheduleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class CoachService {

    private final CoachMapper coachMapper;
    private final CourseScheduleMapper courseScheduleMapper;
    private final JsonUtils jsonUtils;

    public CoachService(CoachMapper coachMapper,
                        CourseScheduleMapper courseScheduleMapper,
                        JsonUtils jsonUtils) {
        this.coachMapper = coachMapper;
        this.courseScheduleMapper = courseScheduleMapper;
        this.jsonUtils = jsonUtils;
    }

    public PageResponse<CoachResponse> page(long pageNo, long pageSize, Integer status, String keyword) {
        LambdaQueryWrapper<Coach> queryWrapper = new LambdaQueryWrapper<Coach>()
            .eq(Coach::getDeleted, 0)
            .orderByDesc(Coach::getId);
        if (status != null) {
            queryWrapper.eq(Coach::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                .like(Coach::getCoachName, keyword)
                .or()
                .like(Coach::getPhone, keyword));
        }
        Page<Coach> page = coachMapper.selectPage(new Page<>(pageNo, pageSize), queryWrapper);
        return new PageResponse<>(page.getRecords().stream().map(this::toResponse).toList(), pageNo, pageSize, page.getTotal());
    }

    @Transactional
    public CoachResponse create(CoachSaveRequest request) {
        Coach coach = new Coach();
        applySaveRequest(coach, request);
        coach.setDeleted(0);
        coachMapper.insert(coach);
        return toResponse(coachMapper.selectById(coach.getId()));
    }

    @Transactional
    public CoachResponse update(Long coachId, CoachSaveRequest request) {
        Coach coach = getCoach(coachId);
        applySaveRequest(coach, request);
        coachMapper.updateById(coach);
        return toResponse(coachMapper.selectById(coachId));
    }

    public CoachWorkloadResponse workload(Long coachId) {
        getCoach(coachId);
        long total = countSchedules(coachId, null);
        long upcoming = courseScheduleMapper.selectCount(new LambdaQueryWrapper<CourseSchedule>()
            .eq(CourseSchedule::getCoachId, coachId)
            .eq(CourseSchedule::getDeleted, 0)
            .ge(CourseSchedule::getStartTime, LocalDateTime.now()));
        long completed = countSchedules(coachId, 1);
        long cancelled = countSchedules(coachId, 2);
        return new CoachWorkloadResponse(coachId, total, upcoming, completed, cancelled);
    }

    public Coach getCoach(Long coachId) {
        Coach coach = coachMapper.selectById(coachId);
        if (coach == null || !Integer.valueOf(0).equals(coach.getDeleted())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "教练不存在");
        }
        return coach;
    }

    private long countSchedules(Long coachId, Integer status) {
        LambdaQueryWrapper<CourseSchedule> queryWrapper = new LambdaQueryWrapper<CourseSchedule>()
            .eq(CourseSchedule::getCoachId, coachId)
            .eq(CourseSchedule::getDeleted, 0);
        if (status != null) {
            queryWrapper.eq(CourseSchedule::getStatus, status);
        }
        return courseScheduleMapper.selectCount(queryWrapper);
    }

    private void applySaveRequest(Coach coach, CoachSaveRequest request) {
        coach.setCoachName(request.coachName());
        coach.setPhone(request.phone());
        coach.setIdCardNo(request.idCardNo());
        coach.setGender(request.gender());
        coach.setSportItems(jsonUtils.toJson(request.sportItems()));
        coach.setIntroduction(request.introduction());
        coach.setHourlyRate(request.hourlyRate());
        coach.setAvailableTimes(jsonUtils.toJson(request.availableTimes()));
        coach.setStatus(request.status() == null ? 1 : request.status());
    }

    private CoachResponse toResponse(Coach coach) {
        return new CoachResponse(
            coach.getId(),
            coach.getCoachName(),
            coach.getPhone(),
            coach.getIdCardNo(),
            coach.getGender(),
            jsonUtils.parseStringList(coach.getSportItems()),
            coach.getIntroduction(),
            coach.getHourlyRate(),
            jsonUtils.parseStringList(coach.getAvailableTimes()),
            coach.getStatus()
        );
    }
}
