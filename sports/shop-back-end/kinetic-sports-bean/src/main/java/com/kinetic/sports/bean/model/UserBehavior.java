package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_behavior")
public class UserBehavior extends BaseEntity {

    private Long userId;

    /** 1=课程 2=商品 */
    private Integer itemType;

    private Long itemId;

    /** view_detail / recommend_click */
    private String behaviorType;

    private String sourcePage;

    private String sourceSection;

    /** 来源对象类型：1=课程 2=商品 */
    private Integer sourceItemType;

    private Long sourceItemId;

    private String extraInfo;
}
