package com.kinetic.sports.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kinetic.sports.bean.model.Order;

import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Order> {

    Order createCourseOrder(Long userId, Order params);

    List<Order> createBatchCourseOrders(Long userId, Order params);

    Order createProdOrder(Long userId, Order params);

    void cancelOrder(Long userId, Long orderId);

    void autoCancelOrder(Long orderId, String reason);

    void payOrder(Long userId, Long orderId);

    void payBatchOrders(Long userId, List<Long> orderIds);

    void payOrderByAdmin(Long orderId);

    void applyRefund(Long userId, Long orderId, String refundReason);

    void approveRefund(Long orderId);

    void rejectRefund(Long orderId);

    void autoRefund(Long orderId, String reason);

    void shipProdOrder(Long orderId);

    void confirmReceive(Long userId, Long orderId);

    void finishOrder(Long orderId);

    Map<String, Object> checkGroupSchedule(Long scheduleId);

    Map<String, Object> previewRefund(Long userId, Long orderId);
}
