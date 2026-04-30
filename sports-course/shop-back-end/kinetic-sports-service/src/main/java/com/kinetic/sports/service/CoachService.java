package com.kinetic.sports.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kinetic.sports.bean.model.Coach;

public interface CoachService extends IService<Coach> {
    Coach getDetailWithStats(Long id);
}
