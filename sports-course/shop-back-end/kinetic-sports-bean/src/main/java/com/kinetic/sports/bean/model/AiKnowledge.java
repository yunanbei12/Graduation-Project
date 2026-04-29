package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_knowledge")
public class AiKnowledge extends BaseEntity {

    private String title;

    private String category;

    private String keywords;

    private String content;

    private Integer priority;

    /** 0=禁用 1=启用 */
    private Integer status;
}
