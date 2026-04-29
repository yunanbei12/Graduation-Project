package com.kinetic.sports.api.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.bean.model.*;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class ApiUserController {

    private final UserCoursePackageService userCoursePackageService;
    private final CourseCheckinService courseCheckinService;
    private final UserCouponService userCouponService;
    private final CouponService couponService;
    private final CourseService courseService;
    private final CoachService coachService;

    /**
     * 我的课包列表
     */
    @GetMapping("/package/list")
    public ServerResponseEntity<List<Map<String, Object>>> myPackages() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<UserCoursePackage> list = userCoursePackageService.list(
                new LambdaQueryWrapper<UserCoursePackage>()
                        .eq(UserCoursePackage::getUserId, userId)
                        .orderByDesc(UserCoursePackage::getCreateTime)
        );
        syncExpiredPackages(list);
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserCoursePackage pkg : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", pkg.getId());
            item.put("courseId", pkg.getCourseId());
            item.put("orderId", pkg.getOrderId());
            item.put("totalLessons", pkg.getTotalLessons());
            item.put("usedLessons", pkg.getUsedLessons());
            item.put("expireTime", pkg.getExpireTime());
            item.put("status", pkg.getStatus());
            item.put("createTime", pkg.getCreateTime());
            Course course = pkg.getCourseId() != null ? courseService.getById(pkg.getCourseId()) : null;
            item.put("courseName", course != null ? course.getName() : "课程包");
            result.add(item);
        }
        return ServerResponseEntity.success(result);
    }

    /**
     * 课包详情
     */
    @GetMapping("/package/detail/{id}")
    public ServerResponseEntity<Map<String, Object>> packageDetail(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        UserCoursePackage pkg = userCoursePackageService.getById(id);
        if (pkg == null || !pkg.getUserId().equals(userId)) {
            return ServerResponseEntity.fail("课包不存在");
        }
        syncExpiredPackage(pkg);
        Map<String, Object> result = new HashMap<>();
        result.put("id", pkg.getId());
        result.put("courseId", pkg.getCourseId());
        result.put("orderId", pkg.getOrderId());
        result.put("totalLessons", pkg.getTotalLessons());
        result.put("usedLessons", pkg.getUsedLessons());
        result.put("expireTime", pkg.getExpireTime());
        result.put("status", pkg.getStatus());
        Course course = pkg.getCourseId() != null ? courseService.getById(pkg.getCourseId()) : null;
        result.put("courseName", course != null ? course.getName() : "课程包");
        return ServerResponseEntity.success(result);
    }

    /**
     * 课包上课记录（附带教练名字）
     */
    @GetMapping("/package/checkin/{packageId}")
    public ServerResponseEntity<List<Map<String, Object>>> packageCheckins(@PathVariable Long packageId) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<CourseCheckin> list = courseCheckinService.list(
                new LambdaQueryWrapper<CourseCheckin>()
                        .eq(CourseCheckin::getUserId, userId)
                        .eq(CourseCheckin::getPackageId, packageId)
                        .orderByDesc(CourseCheckin::getCheckinTime)
        );
        List<Map<String, Object>> result = new ArrayList<>();
        for (CourseCheckin ck : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", ck.getId());
            item.put("checkinTime", ck.getCheckinTime());
            item.put("status", ck.getStatus());
            item.put("location", ck.getLocation());
            item.put("remark", ck.getRemark());
            item.put("checkinType", ck.getCheckinType());
            Coach coach = ck.getCoachId() != null ? coachService.getById(ck.getCoachId()) : null;
            item.put("coachName", coach != null ? coach.getName() : null);
            result.add(item);
        }
        return ServerResponseEntity.success(result);
    }

    /**
     * 我的团课上课记录
     */
    @GetMapping("/checkin/group")
    public ServerResponseEntity<List<CourseCheckin>> groupCheckins() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<CourseCheckin> list = courseCheckinService.list(
                new LambdaQueryWrapper<CourseCheckin>()
                        .eq(CourseCheckin::getUserId, userId)
                        .eq(CourseCheckin::getCheckinType, 2)
                        .orderByDesc(CourseCheckin::getCheckinTime)
        );
        return ServerResponseEntity.success(list);
    }

    /**
     * 可领取的优惠券列表
     */
    @GetMapping("/coupon/available")
    public ServerResponseEntity<List<Coupon>> availableCoupons() {
        Long userId = StpUtil.getLoginIdAsLong();
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> list = couponService.list(
                new LambdaQueryWrapper<Coupon>()
                        .eq(Coupon::getStatus, 1)
                        .le(Coupon::getStartTime, now)
                        .ge(Coupon::getEndTime, now)
        );
        return ServerResponseEntity.success(list);
    }

    /**
     * 领取优惠券
     */
    @PostMapping("/coupon/receive/{couponId}")
    public ServerResponseEntity<Void> receiveCoupon(@PathVariable Long couponId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Coupon coupon = couponService.getById(couponId);
        if (coupon == null || coupon.getStatus() != 1) {
            return ServerResponseEntity.fail("优惠券不可用");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            return ServerResponseEntity.fail("优惠券不在有效期内");
        }
        if (coupon.getTotalCount() > 0 && coupon.getUsedCount() >= coupon.getTotalCount()) {
            return ServerResponseEntity.fail("优惠券已领完");
        }

        // 检查是否已领取
        long count = userCouponService.count(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getCouponId, couponId)
        );
        if (count > 0) {
            return ServerResponseEntity.fail("已领取过该优惠券");
        }

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(0);
        userCouponService.save(userCoupon);

        // 更新已发放数量
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponService.updateById(coupon);

        return ServerResponseEntity.success();
    }

    /**
     * 我的优惠券列表
     */
    @GetMapping("/coupon/list")
    public ServerResponseEntity<List<Map<String, Object>>> myCoupons(
            @RequestParam(required = false) Integer status) {
        Long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId);
        if (status != null) wrapper.eq(UserCoupon::getStatus, status);
        wrapper.orderByDesc(UserCoupon::getCreateTime);
        List<UserCoupon> list = userCouponService.list(wrapper);

        // 附加优惠券详情
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserCoupon uc : list) {
            Coupon coupon = couponService.getById(uc.getCouponId());
            Map<String, Object> item = new HashMap<>();
            item.put("id", uc.getId());
            item.put("couponId", uc.getCouponId());
            item.put("status", uc.getStatus());
            if (coupon != null) {
                item.put("couponName", coupon.getName());
                item.put("couponType", coupon.getType());
                item.put("couponDiscount", coupon.getDiscount());
                item.put("couponDiscountRatio", coupon.getDiscountRatio());
                item.put("couponScope", coupon.getScope());
                item.put("couponMinAmount", coupon.getMinAmount());
                item.put("couponStartTime", coupon.getStartTime());
                item.put("couponEndTime", coupon.getEndTime());
            }
            result.add(item);
        }
        return ServerResponseEntity.success(result);
    }

    /**
     * 下单时可用的优惠券
     */
    @GetMapping("/coupon/usable")
    public ServerResponseEntity<List<Map<String, Object>>> usableCoupons(
            @RequestParam Integer orderType,
            @RequestParam BigDecimal amount) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<UserCoupon> userCoupons = userCouponService.list(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getStatus, 0)
        );

        // 过滤并附加优惠券详情
        List<Map<String, Object>> usable = new ArrayList<>();
        for (UserCoupon uc : userCoupons) {
            Coupon coupon = couponService.getById(uc.getCouponId());
            if (coupon == null || coupon.getStatus() != 1) continue;
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) continue;
            if (coupon.getScope() == 2 && orderType != 1) continue;
            if (coupon.getScope() == 3 && orderType != 2) continue;
            if (amount.compareTo(coupon.getMinAmount()) < 0) continue;

            Map<String, Object> item = new HashMap<>();
            item.put("id", uc.getId());
            item.put("couponId", uc.getCouponId());
            item.put("couponName", coupon.getName());
            item.put("couponType", coupon.getType());
            item.put("couponDiscount", coupon.getDiscount());
            item.put("couponDiscountRatio", coupon.getDiscountRatio());
            item.put("couponScope", coupon.getScope());
            item.put("couponMinAmount", coupon.getMinAmount());
            item.put("couponStartTime", coupon.getStartTime());
            item.put("couponEndTime", coupon.getEndTime());
            usable.add(item);
        }

        return ServerResponseEntity.success(usable);
    }

    private void syncExpiredPackages(List<UserCoursePackage> packages) {
        if (packages == null || packages.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        List<UserCoursePackage> expiredPackages = new ArrayList<>();
        for (UserCoursePackage pkg : packages) {
            if (isExpiredActivePackage(pkg, now)) {
                pkg.setStatus(0);
                expiredPackages.add(pkg);
            }
        }
        if (!expiredPackages.isEmpty()) {
            userCoursePackageService.updateBatchById(expiredPackages);
        }
    }

    private void syncExpiredPackage(UserCoursePackage pkg) {
        if (isExpiredActivePackage(pkg, LocalDateTime.now())) {
            pkg.setStatus(0);
            userCoursePackageService.updateById(pkg);
        }
    }

    private boolean isExpiredActivePackage(UserCoursePackage pkg, LocalDateTime now) {
        return pkg != null
                && pkg.getStatus() != null
                && pkg.getStatus() == 1
                && pkg.getExpireTime() != null
                && !pkg.getExpireTime().isAfter(now);
    }
}
