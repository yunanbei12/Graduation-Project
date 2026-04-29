package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course")
public class Course extends BaseEntity {

    private String name;

    /** 1=私教课 2=团课 */
    private Integer type;

    private Long categoryId;

    private Long coachId;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private String pic;

    private String pics;

    private String description;

    private String detail;

    /** 课程节数（私教课包） */
    private Integer lessonCount;

    /** 特色标签，JSON数组 */
    private String features;

    /** 是否上门授课 */
    private Integer isDoorService;

    /** 课包有效期天数（私教课） */
    private Integer validityDays;

    /** 成团人数（团课） */
    private Integer minGroupSize;

    /** 团课每日开始时间，如 "16:00" */
    private String startHour;

    /** 团课每日结束时间，如 "17:00" */
    private String endHour;

    /** 团课上课地点 */
    private String location;

    /** 团课上课地点图片 */
    private String locationImage;

    /** 团课地点ID */
    private Long locationId;

    /** 教练分成比例(0.5=50%) */
    private BigDecimal settleRatio;

    /** 销量 */
    private Integer sales;

    /** 0=下架 1=上架 */
    private Integer status;
}
