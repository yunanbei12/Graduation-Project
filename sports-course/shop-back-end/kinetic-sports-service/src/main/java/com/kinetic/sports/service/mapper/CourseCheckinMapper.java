package com.kinetic.sports.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kinetic.sports.bean.model.CourseCheckin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CourseCheckinMapper extends BaseMapper<CourseCheckin> {

    @Select("""
            SELECT COUNT(DISTINCT user_id)
            FROM course_checkin
            WHERE coach_id = #{coachId}
              AND status = 1
              AND is_deleted = 0
            """)
    long countDistinctUsersByCoachId(@Param("coachId") Long coachId);
}
