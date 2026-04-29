package com.kinetic.sports.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.bean.model.Order;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/list")
    public ServerResponseEntity<Page<Order>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer orderType,
            @RequestParam(required = false) Integer status) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (orderType != null) wrapper.eq(Order::getOrderType, orderType);
        if (status != null) wrapper.eq(Order::getStatus, status);
        wrapper.orderByDesc(Order::getCreateTime);
        return ServerResponseEntity.success(orderService.page(page, wrapper));
    }

    @GetMapping("/detail/{id}")
    public ServerResponseEntity<Order> detail(@PathVariable Long id) {
        return ServerResponseEntity.success(orderService.getById(id));
    }

    @PutMapping("/status")
    public ServerResponseEntity<Void> updateStatus(@RequestBody Order order) {
        Order dbOrder = orderService.getById(order.getId());
        if (dbOrder == null) {
            return ServerResponseEntity.fail("订单不存在");
        }

        Integer targetStatus = order.getStatus();
        if (targetStatus == null) {
            return ServerResponseEntity.fail("目标状态不能为空");
        }

        if (targetStatus == 2 && dbOrder.getStatus() == 1) {
            orderService.payOrderByAdmin(dbOrder.getId());
            return ServerResponseEntity.success();
        }
        if (targetStatus == 4 && dbOrder.getStatus() == 3) {
            orderService.finishOrder(dbOrder.getId());
            return ServerResponseEntity.success();
        }
        if (targetStatus == 7 && dbOrder.getStatus() == 6) {
            orderService.approveRefund(dbOrder.getId());
            return ServerResponseEntity.success();
        }
        if (targetStatus == 8 && dbOrder.getStatus() == 6) {
            orderService.rejectRefund(dbOrder.getId());
            return ServerResponseEntity.success();
        }

        return ServerResponseEntity.fail("当前状态不支持该操作");
    }
}
