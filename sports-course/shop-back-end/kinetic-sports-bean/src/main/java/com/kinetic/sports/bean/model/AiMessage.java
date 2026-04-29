package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_message")
public class AiMessage extends BaseEntity {

    private Long sessionId;

    private Long userId;

    /** user / assistant */
    private String role;

    private String content;

    private String intent;

    private BigDecimal confidence;

    private String replyText;

    private String cardsJson;

    private String actionsJson;

    /** rule / model / fallback */
    private String sourceType;

    /** 0=否 1=是 */
    private Integer hitRule;

    /** 0=否 1=是 */
    private Integer needHandover;
}
