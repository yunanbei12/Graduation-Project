package com.kinetic.sports.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.bean.model.CoachSettlement;
import com.kinetic.sports.bean.model.CourseCheckin;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.CoachSettlementService;
import com.kinetic.sports.service.CourseCheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final CourseCheckinService courseCheckinService;
    private final CoachSettlementService coachSettlementService;

    /**
     * 教练结算 - 发起结算
     */
    @PostMapping("/settle")
    public ServerResponseEntity<Void> createSettlement(@RequestBody CoachSettlement settlement) {
        // 查询该教练在指定周期内未结算的消课记录
        LocalDateTime startDateTime = settlement.getPeriodStart().atStartOfDay();
        LocalDateTime endDateTime = settlement.getPeriodEnd().atTime(LocalTime.MAX);

        List<CourseCheckin> unsettled = courseCheckinService.list(
                new LambdaQueryWrapper<CourseCheckin>()
                        .eq(CourseCheckin::getCoachId, settlement.getCoachId())
                        .eq(CourseCheckin::getSettleStatus, 0)
                        .eq(CourseCheckin::getStatus, 1)
                        .between(CourseCheckin::getCheckinTime, startDateTime, endDateTime)
        );

        if (unsettled.isEmpty()) {
            return ServerResponseEntity.fail("无待结算记录");
        }

        // 汇总
        int totalLessons = unsettled.size();
        BigDecimal totalAmount = unsettled.stream()
                .map(CourseCheckin::getCoachAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        settlement.setTotalLessons(totalLessons);
        settlement.setTotalAmount(totalAmount);
        settlement.setStatus(0);
        coachSettlementService.save(settlement);

        // 标记消课记录为已结算
        for (CourseCheckin c : unsettled) {
            c.setSettleStatus(1);
            courseCheckinService.updateById(c);
        }

        return ServerResponseEntity.success();
    }

    /**
     * 结算列表
     */
    @GetMapping("/settlement/list")
    public ServerResponseEntity<Page<CoachSettlement>> settlementList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long coachId) {
        Page<CoachSettlement> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CoachSettlement> wrapper = new LambdaQueryWrapper<>();
        if (coachId != null) wrapper.eq(CoachSettlement::getCoachId, coachId);
        wrapper.orderByDesc(CoachSettlement::getCreateTime);
        return ServerResponseEntity.success(coachSettlementService.page(page, wrapper));
    }

    /**
     * 确认结算
     */
    @PutMapping("/settle/confirm/{id}")
    public ServerResponseEntity<Void> confirmSettlement(@PathVariable Long id) {
        CoachSettlement settlement = coachSettlementService.getById(id);
        if (settlement == null) {
            return ServerResponseEntity.fail("结算记录不存在");
        }
        settlement.setStatus(1);
        settlement.setSettleTime(LocalDateTime.now());
        coachSettlementService.updateById(settlement);
        return ServerResponseEntity.success();
    }

    /**
     * 待结算汇总（按教练）
     */
    @GetMapping("/unsettled/summary")
    public ServerResponseEntity<?> unsettledSummary() {
        // 简化版：返回所有未结算的消课记录汇总
        List<CourseCheckin> unsettled = courseCheckinService.list(
                new LambdaQueryWrapper<CourseCheckin>()
                        .eq(CourseCheckin::getSettleStatus, 0)
                        .eq(CourseCheckin::getStatus, 1)
        );
        return ServerResponseEntity.success(unsettled);
    }
}
