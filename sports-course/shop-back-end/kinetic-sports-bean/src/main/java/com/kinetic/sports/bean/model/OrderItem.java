package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_item")
public class OrderItem extends BaseEntity {

    private Long orderId;

    /** 课程版固定使用 1=课程 */
    private Integer itemType;

    private Long itemId;

    private String itemName;

    private String itemPic;

    private BigDecimal price;

    private Integer qty;

}
