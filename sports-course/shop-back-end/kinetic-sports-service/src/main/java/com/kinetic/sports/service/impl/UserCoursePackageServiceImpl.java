package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.model.UserCoursePackage;
import com.kinetic.sports.service.UserCoursePackageService;
import com.kinetic.sports.service.mapper.UserCoursePackageMapper;
import org.springframework.stereotype.Service;

@Service
public class UserCoursePackageServiceImpl extends ServiceImpl<UserCoursePackageMapper, UserCoursePackage> implements UserCoursePackageService {
}
