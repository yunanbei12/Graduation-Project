package com.kinetic.sports.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kinetic.sports.bean.model.CourseCheckin;

public interface CourseCheckinService extends IService<CourseCheckin> {
    long countDistinctUsersByCoachId(Long coachId);
}
