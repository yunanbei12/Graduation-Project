package com.kinetic.sports.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kinetic.sports.bean.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
