package com.kinetic.sports.bean.vo.recommend;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RecommendHomeVO {

    private List<RecommendItemVO> courseList = new ArrayList<>();

    private List<RecommendItemVO> prodList = new ArrayList<>();
}
