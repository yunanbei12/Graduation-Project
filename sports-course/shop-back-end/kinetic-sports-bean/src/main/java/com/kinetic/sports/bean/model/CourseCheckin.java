package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_checkin")
public class CourseCheckin extends BaseEntity {

    private Long userId;

    private Long courseId;

    private Long packageId;

    private Long scheduleId;

    private Long coachId;

    private String location;

    private LocalDateTime checkinTime;

    /** 1=私教课消课 2=团课签到 */
    private Integer checkinType;

    /** 0=缺勤 1=正常出勤 */
    private Integer status;

    /** 单节课价格快照 */
    private BigDecimal lessonPrice;

    /** 教练分成比例快照(0.5=50%) */
    private BigDecimal coachRatio;

    /** 教练分成金额(自动计算) */
    private BigDecimal coachAmount;

    /** 0=未结算 1=已结算 */
    private Integer settleStatus;

    private String remark;
}
