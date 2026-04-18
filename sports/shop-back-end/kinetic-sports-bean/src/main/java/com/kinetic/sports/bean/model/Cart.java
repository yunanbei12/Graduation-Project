package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cart")
public class Cart extends BaseEntity {

    private Long userId;

    private Long prodId;

    private Long skuId;

    private Integer qty;

    /** 非数据库字段 - 商品名称 */
    @TableField(exist = false)
    private String prodName;

    /** 非数据库字段 - 商品图片 */
    @TableField(exist = false)
    private String prodPic;

    /** 非数据库字段 - SKU规格 */
    @TableField(exist = false)
    private String skuProperties;

    /** 非数据库字段 - SKU价格 */
    @TableField(exist = false)
    private BigDecimal skuPrice;
}
