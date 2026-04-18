package com.kinetic.sports.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kinetic.sports.bean.model.Cart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {
}
