package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_handover")
public class AiHandover extends BaseEntity {

    private Long sessionId;

    private Long userId;

    private String latestQuestion;

    /** 0=待处理 1=已处理 */
    private Integer status;

    private String adminRemark;

    private String handledBy;

    private LocalDateTime handledTime;
}
