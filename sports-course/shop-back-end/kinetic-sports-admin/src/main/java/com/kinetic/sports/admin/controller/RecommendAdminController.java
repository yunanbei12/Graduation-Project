package com.kinetic.sports.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.bean.model.UserBehavior;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.UserBehaviorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/recommend")
@RequiredArgsConstructor
public class RecommendAdminController {

    private final UserBehaviorService userBehaviorService;

    @GetMapping("/stats/summary")
    public ServerResponseEntity<Map<String, Object>> statsSummary() {
        return ServerResponseEntity.success(userBehaviorService.getRecommendStatsSummary());
    }

    @GetMapping("/behavior/list")
    public ServerResponseEntity<Page<Map<String, Object>>> behaviorList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer itemType,
            @RequestParam(required = false) String behaviorType,
            @RequestParam(required = false) String sourceSection) {
        return ServerResponseEntity.success(userBehaviorService.getBehaviorPage(
                new Page<UserBehavior>(pageNum, pageSize), itemType, behaviorType, sourceSection));
    }
}
