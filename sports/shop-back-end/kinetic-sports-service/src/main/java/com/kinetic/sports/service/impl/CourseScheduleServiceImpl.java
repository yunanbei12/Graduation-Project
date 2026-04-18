package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.model.CourseSchedule;
import com.kinetic.sports.service.CourseScheduleService;
import com.kinetic.sports.service.mapper.CourseScheduleMapper;
import org.springframework.stereotype.Service;

@Service
public class CourseScheduleServiceImpl extends ServiceImpl<CourseScheduleMapper, CourseSchedule> implements CourseScheduleService {
}
