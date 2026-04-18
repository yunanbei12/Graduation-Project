package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coach_settlement")
public class CoachSettlement extends BaseEntity {

    private Long coachId;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private Integer totalLessons;

    private BigDecimal totalAmount;

    /** 0=待结算 1=已结算 */
    private Integer status;

    private LocalDateTime settleTime;

    private String remark;
}
