package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.model.CourseCategory;
import com.kinetic.sports.service.CourseCategoryService;
import com.kinetic.sports.service.mapper.CourseCategoryMapper;
import org.springframework.stereotype.Service;

@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {
}
