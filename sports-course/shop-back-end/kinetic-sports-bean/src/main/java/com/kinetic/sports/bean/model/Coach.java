package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coach")
public class Coach extends BaseEntity {

    private String name;

    private String englishName;

    /** 头像(列表卡片展示) */
    private String avatar;

    /** 详情图片(详情页大图) */
    private String pic;

    /** 联系电话(后台展示) */
    private String phone;

    private Integer years;

    private Double rating;

    private String bio;

    private String certs;

    private String skills;

    private Integer status;

    /** 非数据库字段：累计服务学员人数 */
    @TableField(exist = false)
    private Long servedUserCount;
}
