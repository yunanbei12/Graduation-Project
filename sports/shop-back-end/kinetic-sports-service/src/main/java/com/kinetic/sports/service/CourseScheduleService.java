package com.kinetic.sports.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kinetic.sports.bean.model.BatchScheduleCreateCommand;
import com.kinetic.sports.bean.model.CourseSchedule;

public interface CourseScheduleService extends IService<CourseSchedule> {

    void saveGroupSchedule(CourseSchedule schedule);

    int saveBatchGroupSchedules(BatchScheduleCreateCommand command);

    void updateGroupSchedule(CourseSchedule schedule);
}
