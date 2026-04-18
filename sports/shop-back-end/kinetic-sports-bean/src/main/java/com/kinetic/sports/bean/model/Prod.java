package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prod")
public class Prod extends BaseEntity {

    private String name;

    private Long categoryId;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private String pic;

    private String pics;

    private String description;

    private String detail;

    private Integer sales;

    /** 0=下架 1=上架 */
    private Integer status;
}
