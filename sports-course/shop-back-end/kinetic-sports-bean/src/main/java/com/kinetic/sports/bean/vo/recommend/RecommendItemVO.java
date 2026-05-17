package com.kinetic.sports.bean.vo.recommend;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecommendItemVO {

    private Long id;

    /** course / prod */
    private String bizType;

    private String name;

    private String pic;

    private String locationImage;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer sales;

    private Long categoryId;

    private String reason;

    /** 课程类型：1=私教课 2=团课 */
    private Integer courseType;

    private Integer lessonCount;

    private Integer minGroupSize;

    private Integer availableScheduleCount;

    private String nextScheduleText;
}
