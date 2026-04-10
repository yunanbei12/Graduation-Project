package com.sportedu.backend.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sportedu.backend.teaching.entity.CourseSchedule;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseScheduleMapper extends BaseMapper<CourseSchedule> {
}
