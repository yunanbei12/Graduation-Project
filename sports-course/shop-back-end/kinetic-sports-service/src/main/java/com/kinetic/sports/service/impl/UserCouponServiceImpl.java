package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.model.UserCoupon;
import com.kinetic.sports.service.UserCouponService;
import com.kinetic.sports.service.mapper.UserCouponMapper;
import org.springframework.stereotype.Service;

@Service
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements UserCouponService {
}
