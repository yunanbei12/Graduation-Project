package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sku")
public class Sku extends BaseEntity {

    private Long prodId;

    private String properties;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer stocks;

    private String pic;
}
