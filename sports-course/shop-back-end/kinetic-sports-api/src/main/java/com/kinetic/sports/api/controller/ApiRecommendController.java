package com.kinetic.sports.api.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.kinetic.sports.bean.vo.recommend.RecommendHomeVO;
import com.kinetic.sports.bean.vo.recommend.RecommendItemVO;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recommend")
@RequiredArgsConstructor
public class ApiRecommendController {

    private final RecommendService recommendService;

    @GetMapping("/home")
    public ServerResponseEntity<RecommendHomeVO> home(@RequestParam(defaultValue = "4") Integer courseLimit) {
        Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        return ServerResponseEntity.success(recommendService.recommendHome(userId, courseLimit));
    }

    @GetMapping("/course")
    public ServerResponseEntity<List<RecommendItemVO>> course(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "6") Integer limit) {
        Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        return ServerResponseEntity.success(recommendService.recommendCourses(userId, type, categoryId, null, limit));
    }

    @GetMapping("/course/related/{courseId}")
    public ServerResponseEntity<List<RecommendItemVO>> relatedCourse(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "4") Integer limit) {
        Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        return ServerResponseEntity.success(recommendService.recommendCourses(userId, null, null, courseId, limit));
    }
}
