package com.kinetic.sports.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kinetic.sports.bean.model.CourseCheckin;

import java.util.List;

public interface CourseCheckinService extends IService<CourseCheckin> {
    long countDistinctUsersByCoachId(Long coachId);

    void settleGroupSchedule(Long scheduleId, List<Long> absentUserIds);

    boolean hasGroupCheckins(Long scheduleId);
}
