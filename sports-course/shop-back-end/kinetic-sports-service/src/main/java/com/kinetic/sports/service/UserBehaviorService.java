package com.kinetic.sports.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kinetic.sports.bean.model.UserBehavior;

import java.util.Map;

public interface UserBehaviorService extends IService<UserBehavior> {

    void trackBehavior(Long userId, UserBehavior behavior);

    Map<String, Object> getRecommendStatsSummary();

    Page<Map<String, Object>> getBehaviorPage(Page<UserBehavior> page,
                                              Integer itemType,
                                              String behaviorType,
                                              String sourceSection);
}
