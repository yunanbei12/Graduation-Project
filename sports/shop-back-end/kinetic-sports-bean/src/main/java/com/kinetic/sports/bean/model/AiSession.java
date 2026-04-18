package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_session")
public class AiSession extends BaseEntity {

    private Long userId;

    private String title;

    private String lastQuestion;

    private String lastReply;

    private String lastIntent;

    /** 0=处理中 1=已解决 2=待人工 3=本轮已结束 */
    private Integer status;

    /** 0=否 1=是 */
    private Integer needHandover;

    private LocalDateTime lastMessageTime;

    private LocalDateTime resolvedTime;
}
