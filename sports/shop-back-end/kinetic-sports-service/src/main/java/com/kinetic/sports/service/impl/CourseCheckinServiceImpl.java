package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.model.CourseCheckin;
import com.kinetic.sports.service.CourseCheckinService;
import com.kinetic.sports.service.mapper.CourseCheckinMapper;
import org.springframework.stereotype.Service;

@Service
public class CourseCheckinServiceImpl extends ServiceImpl<CourseCheckinMapper, CourseCheckin> implements CourseCheckinService {
}
