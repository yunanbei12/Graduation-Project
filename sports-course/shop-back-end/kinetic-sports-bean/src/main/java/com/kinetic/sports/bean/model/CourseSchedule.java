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

    /** 排课开始时间快照，便于排序、展示和定时任务直接使用 */
    private LocalDateTime startTime;

    /** 排课结束时间快照，便于排序、展示和定时任务直接使用 */
    private LocalDateTime endTime;

    /** 排课地点快照，通常与课程模板地点保持一致 */
    private String location;

    private Integer totalSeats;

    private Integer enrolledSeats;

    /** 0=未开始 1=进行中 2=已结束 3=已取消 */
    private Integer status;
}
