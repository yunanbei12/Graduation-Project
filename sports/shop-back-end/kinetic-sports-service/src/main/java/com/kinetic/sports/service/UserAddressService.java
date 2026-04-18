package com.kinetic.sports.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kinetic.sports.bean.model.UserAddress;

import java.util.List;

public interface UserAddressService extends IService<UserAddress> {

    /**
     * 获取用户所有地址
     */
    List<UserAddress> listByUserId(Long userId);

    /**
     * 获取用户默认地址
     */
    UserAddress getDefaultAddress(Long userId);

    /**
     * 设置默认地址（会先清除其他默认地址）
     */
    void setDefaultAddress(Long userId, Long addressId);
}
