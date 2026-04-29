package com.kinetic.sports.service;

import com.kinetic.sports.bean.vo.recommend.RecommendHomeVO;
import com.kinetic.sports.bean.vo.recommend.RecommendItemVO;

import java.util.List;

public interface RecommendService {

    RecommendHomeVO recommendHome(Long userId, Integer courseLimit);

    List<RecommendItemVO> recommendCourses(Long userId, Integer type, Long categoryId, Long currentCourseId, Integer limit);
}
