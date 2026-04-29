package com.kinetic.sports.api.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.kinetic.sports.bean.model.UserBehavior;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.UserBehaviorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/behavior")
@RequiredArgsConstructor
public class ApiBehaviorController {

    private final UserBehaviorService userBehaviorService;

    @PostMapping("/view")
    public ServerResponseEntity<Void> view(@RequestBody UserBehavior behavior) {
        userBehaviorService.trackBehavior(StpUtil.getLoginIdAsLong(), behaviorWithType(behavior, "view_detail"));
        return ServerResponseEntity.success();
    }

    @PostMapping("/recommend-click")
    public ServerResponseEntity<Void> recommendClick(@RequestBody UserBehavior behavior) {
        userBehaviorService.trackBehavior(StpUtil.getLoginIdAsLong(), behaviorWithType(behavior, "recommend_click"));
        return ServerResponseEntity.success();
    }

    private UserBehavior behaviorWithType(UserBehavior behavior, String behaviorType) {
        if (behavior == null) {
            behavior = new UserBehavior();
        }
        behavior.setBehaviorType(behaviorType);
        return behavior;
    }
}
