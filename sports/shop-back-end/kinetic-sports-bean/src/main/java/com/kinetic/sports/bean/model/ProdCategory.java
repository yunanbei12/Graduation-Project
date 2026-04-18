package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prod_category")
public class ProdCategory extends BaseEntity {

    private String name;

    private String icon;

    private Integer sort;

    private Integer status;
}
