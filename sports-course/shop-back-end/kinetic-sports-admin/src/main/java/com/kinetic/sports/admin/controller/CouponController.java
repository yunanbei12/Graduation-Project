package com.kinetic.sports.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.bean.model.Coupon;
import com.kinetic.sports.bean.model.User;
import com.kinetic.sports.bean.model.UserCoupon;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.CouponService;
import com.kinetic.sports.service.UserCouponService;
import com.kinetic.sports.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final UserService userService;
    private final UserCouponService userCouponService;

    @GetMapping("/list")
    public ServerResponseEntity<Page<Coupon>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Coupon> page = new Page<>(pageNum, pageSize);
        return ServerResponseEntity.success(couponService.page(page,
                new LambdaQueryWrapper<Coupon>().orderByDesc(Coupon::getCreateTime)));
    }

    @GetMapping("/detail/{id}")
    public ServerResponseEntity<Coupon> detail(@PathVariable Long id) {
        return ServerResponseEntity.success(couponService.getById(id));
    }

    @PostMapping
    public ServerResponseEntity<Void> save(@RequestBody Coupon coupon) {
        couponService.save(coupon);
        return ServerResponseEntity.success();
    }

    @PutMapping
    public ServerResponseEntity<Void> update(@RequestBody Coupon coupon) {
        couponService.updateById(coupon);
        return ServerResponseEntity.success();
    }

    @DeleteMapping("/{id}")
    public ServerResponseEntity<Void> delete(@PathVariable Long id) {
        couponService.removeById(id);
        return ServerResponseEntity.success();
    }

    /**
     * 获取可发放的用户列表
     */
    @GetMapping("/send/users")
    public ServerResponseEntity<Page<User>> users(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getNickName, keyword)
                    .or().like(User::getPhone, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return ServerResponseEntity.success(userService.page(page, wrapper));
    }

    /**
     * 指定用户发放
     */
    @PostMapping("/send/{couponId}/user/{userId}")
    public ServerResponseEntity<Void> sendToUser(@PathVariable Long couponId, @PathVariable Long userId) {
        Coupon coupon = couponService.getById(couponId);
        if (coupon == null || coupon.getStatus() != 1) {
            return ServerResponseEntity.fail("优惠券不可用");
        }
        if (coupon.getTotalCount() > 0 && coupon.getUsedCount() >= coupon.getTotalCount()) {
            return ServerResponseEntity.fail("优惠券已发完");
        }
        // 检查是否已发放
        long count = userCouponService.count(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId));
        if (count > 0) {
            return ServerResponseEntity.fail("该用户已有此优惠券");
        }
        // 发放
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(0);
        userCouponService.save(userCoupon);
        // 更新发放数量
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponService.updateById(coupon);
        return ServerResponseEntity.success();
    }

    /**
     * 批量发放给所有用户
     */
    @PostMapping("/send/{couponId}/all")
    public ServerResponseEntity<Map<String, Integer>> sendToAll(@PathVariable Long couponId) {
        Coupon coupon = couponService.getById(couponId);
        if (coupon == null || coupon.getStatus() != 1) {
            return ServerResponseEntity.fail("优惠券不可用");
        }
        // 获取所有用户
        List<User> users = userService.list();
        // 已领取的用户
        List<UserCoupon> existing = userCouponService.list(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getCouponId, couponId));
        List<Long> existingUserIds = existing.stream()
                .map(UserCoupon::getUserId)
                .collect(Collectors.toList());
        // 过滤未领取的用户
        List<UserCoupon> newUserCoupons = new ArrayList<>();
        for (User user : users) {
            if (!existingUserIds.contains(user.getId())) {
                // 检查发放总量
                if (coupon.getTotalCount() > 0 && coupon.getUsedCount() + newUserCoupons.size() >= coupon.getTotalCount()) {
                    break;
                }
                UserCoupon uc = new UserCoupon();
                uc.setUserId(user.getId());
                uc.setCouponId(couponId);
                uc.setStatus(0);
                newUserCoupons.add(uc);
            }
        }
        if (!newUserCoupons.isEmpty()) {
            userCouponService.saveBatch(newUserCoupons);
            coupon.setUsedCount(coupon.getUsedCount() + newUserCoupons.size());
            couponService.updateById(coupon);
        }
        return ServerResponseEntity.success(Map.of("success", newUserCoupons.size()));
    }

    /**
     * 批量发放给新用户（近30天注册）
     */
    @PostMapping("/send/{couponId}/new")
    public ServerResponseEntity<Map<String, Integer>> sendToNew(@PathVariable Long couponId) {
        Coupon coupon = couponService.getById(couponId);
        if (coupon == null || coupon.getStatus() != 1) {
            return ServerResponseEntity.fail("优惠券不可用");
        }
        // 近30天注册的用户
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<User> newUsers = userService.list(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, thirtyDaysAgo));
        // 已领取的用户
        List<UserCoupon> existing = userCouponService.list(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getCouponId, couponId));
        List<Long> existingUserIds = existing.stream()
                .map(UserCoupon::getUserId)
                .collect(Collectors.toList());
        // 过滤未领取的用户
        List<UserCoupon> newUserCoupons = new ArrayList<>();
        for (User user : newUsers) {
            if (!existingUserIds.contains(user.getId())) {
                if (coupon.getTotalCount() > 0 && coupon.getUsedCount() + newUserCoupons.size() >= coupon.getTotalCount()) {
                    break;
                }
                UserCoupon uc = new UserCoupon();
                uc.setUserId(user.getId());
                uc.setCouponId(couponId);
                uc.setStatus(0);
                newUserCoupons.add(uc);
            }
        }
        if (!newUserCoupons.isEmpty()) {
            userCouponService.saveBatch(newUserCoupons);
            coupon.setUsedCount(coupon.getUsedCount() + newUserCoupons.size());
            couponService.updateById(coupon);
        }
        return ServerResponseEntity.success(Map.of("success", newUserCoupons.size()));
    }
}
