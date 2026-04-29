package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_course_package")
public class UserCoursePackage extends BaseEntity {

    private Long userId;

    private Long courseId;

    private Long orderId;

    private Integer totalLessons;

    private Integer usedLessons;

    private LocalDateTime expireTime;

    /** 0=已过期 1=正常 2=已退费 3=已完成 */
    private Integer status;
}
