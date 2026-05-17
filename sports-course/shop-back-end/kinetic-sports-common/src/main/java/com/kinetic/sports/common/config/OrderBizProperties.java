package com.kinetic.sports.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "biz.order")
public class OrderBizProperties {

    /**
     * 是否启用 RabbitMQ 订单事件链路
     */
    private Boolean rabbitEnabled = false;

    /**
     * 待支付订单自动关闭时间，单位分钟
     */
    private Integer closeTimeoutMinutes = 15;

    /**
     * 团课自动成团检查提前时间，单位分钟；0 表示开课时判断
     */
    private Integer groupCheckBeforeMinutes = 0;
}
