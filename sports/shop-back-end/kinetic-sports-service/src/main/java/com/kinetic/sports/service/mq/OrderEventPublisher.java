package com.kinetic.sports.service.mq;

import com.kinetic.sports.bean.dto.order.GroupScheduleCheckMessage;
import com.kinetic.sports.bean.dto.order.OrderDelayMessage;
import com.kinetic.sports.bean.dto.order.OrderLifecycleMessage;
import com.kinetic.sports.common.config.OrderBizProperties;
import com.kinetic.sports.common.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final OrderBizProperties orderBizProperties;

    public void publishOrderCloseDelay(Long orderId) {
        if (!Boolean.TRUE.equals(orderBizProperties.getRabbitEnabled())) {
            return;
        }
        rabbitTemplate.convertAndSend(
                RabbitConfig.ORDER_EVENT_EXCHANGE,
                RabbitConfig.ORDER_CLOSE_DELAY_ROUTING_KEY,
                new OrderDelayMessage(orderId)
        );
    }

    public void publishGroupCheckDelay(Long scheduleId, long delayMillis) {
        if (!Boolean.TRUE.equals(orderBizProperties.getRabbitEnabled())) {
            return;
        }
        long safeDelay = Math.max(delayMillis, 1000L);
        rabbitTemplate.convertAndSend(
                RabbitConfig.GROUP_EVENT_EXCHANGE,
                RabbitConfig.GROUP_CHECK_DELAY_ROUTING_KEY,
                new GroupScheduleCheckMessage(scheduleId),
                message -> {
                    message.getMessageProperties().setExpiration(String.valueOf(safeDelay));
                    return message;
                }
        );
    }

    public void publishOrderPaid(OrderLifecycleMessage message) {
        if (!Boolean.TRUE.equals(orderBizProperties.getRabbitEnabled())) {
            return;
        }
        rabbitTemplate.convertAndSend(
                RabbitConfig.ORDER_EVENT_EXCHANGE,
                RabbitConfig.ORDER_PAID_ROUTING_KEY,
                message
        );
    }

    public void publishOrderRefunded(OrderLifecycleMessage message) {
        if (!Boolean.TRUE.equals(orderBizProperties.getRabbitEnabled())) {
            return;
        }
        rabbitTemplate.convertAndSend(
                RabbitConfig.ORDER_EVENT_EXCHANGE,
                RabbitConfig.ORDER_REFUNDED_ROUTING_KEY,
                message
        );
    }
}
