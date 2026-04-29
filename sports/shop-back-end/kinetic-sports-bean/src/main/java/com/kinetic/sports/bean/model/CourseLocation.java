package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_location")
public class CourseLocation extends BaseEntity {

    private String name;

    private String coverImage;

    private String address;

    private String description;

    private Integer sort;

    private Integer status;
}
