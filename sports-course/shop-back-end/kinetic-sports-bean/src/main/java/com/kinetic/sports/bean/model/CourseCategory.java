package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_category")
public class CourseCategory extends BaseEntity {

    private String name;

    private String icon;

    private Integer sort;

    private Integer status;
}
