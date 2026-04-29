package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.model.Coach;
import com.kinetic.sports.service.CoachService;
import com.kinetic.sports.service.mapper.CoachMapper;
import org.springframework.stereotype.Service;

@Service
public class CoachServiceImpl extends ServiceImpl<CoachMapper, Coach> implements CoachService {
}
