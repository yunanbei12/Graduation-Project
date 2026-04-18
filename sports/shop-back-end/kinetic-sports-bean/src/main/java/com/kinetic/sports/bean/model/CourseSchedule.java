package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_schedule")
public class CourseSchedule extends BaseEntity {

    private Long courseId;

    private Long coachId;

    /** 团课排课日期，具体时间段从课程模板读取 */
    private LocalDate scheduleDate;

    /** 兼容旧数据，新数据只用 scheduleDate */
    private LocalDateTime startTime;

    /** 兼容旧数据 */
    private LocalDateTime endTime;

    /** 兼容旧数据，地点现已移至课程模板 */
    private String location;

    private Integer totalSeats;

    private Integer enrolledSeats;

    /** 0=未开始 1=进行中 2=已结束 3=已取消 */
    private Integer status;
}
