package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_feedback")
public class AiFeedback extends BaseEntity {

    private Long sessionId;

    private Long messageId;

    private Long userId;

    /** 1=有帮助 0=无帮助 */
    private Integer rating;

    private String comment;
}
