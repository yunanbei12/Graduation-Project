package com.kinetic.sports.api.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kinetic.sports.bean.model.UserAddress;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class ApiUserAddressController {

    private final UserAddressService userAddressService;

    /**
     * 获取当前用户所有地址
     */
    @GetMapping("/list")
    public ServerResponseEntity<List<UserAddress>> list() {
        Long userId = StpUtil.getLoginIdAsLong();
        return ServerResponseEntity.success(userAddressService.listByUserId(userId));
    }

    /**
     * 获取地址详情
     */
    @GetMapping("/detail/{id}")
    public ServerResponseEntity<UserAddress> detail(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        UserAddress address = userAddressService.getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return ServerResponseEntity.fail("地址不存在");
        }
        return ServerResponseEntity.success(address);
    }

    /**
     * 获取默认地址
     */
    @GetMapping("/default")
    public ServerResponseEntity<UserAddress> getDefault() {
        Long userId = StpUtil.getLoginIdAsLong();
        return ServerResponseEntity.success(userAddressService.getDefaultAddress(userId));
    }

    /**
     * 新增地址
     */
    @PostMapping
    public ServerResponseEntity<Void> add(@RequestBody UserAddress address) {
        Long userId = StpUtil.getLoginIdAsLong();
        address.setUserId(userId);
        address.setIsDefault(0);
        userAddressService.save(address);
        // 如果是第一个地址，自动设为默认
        long count = userAddressService.count(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId));
        if (count == 1) {
            userAddressService.setDefaultAddress(userId, address.getId());
        }
        return ServerResponseEntity.success();
    }

    /**
     * 修改地址
     */
    @PutMapping
    public ServerResponseEntity<Void> update(@RequestBody UserAddress address) {
        Long userId = StpUtil.getLoginIdAsLong();
        UserAddress existing = userAddressService.getById(address.getId());
        if (existing == null || !existing.getUserId().equals(userId)) {
            return ServerResponseEntity.fail("地址不存在");
        }
        address.setUserId(userId);
        userAddressService.updateById(address);
        return ServerResponseEntity.success();
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/{id}")
    public ServerResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        UserAddress address = userAddressService.getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return ServerResponseEntity.fail("地址不存在");
        }
        userAddressService.removeById(id);
        return ServerResponseEntity.success();
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/default/{id}")
    public ServerResponseEntity<Void> setDefault(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        UserAddress address = userAddressService.getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return ServerResponseEntity.fail("地址不存在");
        }
        userAddressService.setDefaultAddress(userId, id);
        return ServerResponseEntity.success();
    }
}
