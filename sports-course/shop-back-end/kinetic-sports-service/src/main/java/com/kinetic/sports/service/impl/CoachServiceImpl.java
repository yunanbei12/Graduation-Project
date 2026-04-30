package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.model.Coach;
import com.kinetic.sports.service.CourseCheckinService;
import com.kinetic.sports.service.CoachService;
import com.kinetic.sports.service.mapper.CoachMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CoachServiceImpl extends ServiceImpl<CoachMapper, Coach> implements CoachService {

    private final CourseCheckinService courseCheckinService;

    @Override
    public Coach getDetailWithStats(Long id) {
        Coach coach = getById(id);
        if (coach == null) {
            return null;
        }
        coach.setServedUserCount(courseCheckinService.countDistinctUsersByCoachId(id));
        return coach;
    }
}
