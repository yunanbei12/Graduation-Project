package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.model.CourseLocation;
import com.kinetic.sports.service.CourseLocationService;
import com.kinetic.sports.service.mapper.CourseLocationMapper;
import org.springframework.stereotype.Service;

@Service
public class CourseLocationServiceImpl extends ServiceImpl<CourseLocationMapper, CourseLocation> implements CourseLocationService {
}
