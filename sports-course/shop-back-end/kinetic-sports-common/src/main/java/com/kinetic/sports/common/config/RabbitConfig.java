package com.kinetic.sports.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
@ConditionalOnProperty(prefix = "biz.order", name = "rabbit-enabled", havingValue = "true")
public class RabbitConfig {

    public static final String ORDER_EVENT_EXCHANGE = "kinetic.order.event.exchange";
    public static final String GROUP_EVENT_EXCHANGE = "kinetic.group.event.exchange";

    public static final String ORDER_CLOSE_DELAY_QUEUE = "kinetic.order.close.delay.queue";
    public static final String ORDER_CLOSE_PROCESS_QUEUE = "kinetic.order.close.process.queue";
    public static final String GROUP_CHECK_DELAY_QUEUE = "kinetic.group.check.delay.queue";
    public static final String GROUP_CHECK_PROCESS_QUEUE = "kinetic.group.check.process.queue";

    public static final String ORDER_CLOSE_DELAY_ROUTING_KEY = "order.close.delay";
    public static final String ORDER_CLOSE_PROCESS_ROUTING_KEY = "order.close.process";
    public static final String GROUP_CHECK_DELAY_ROUTING_KEY = "group.check.delay";
    public static final String GROUP_CHECK_PROCESS_ROUTING_KEY = "group.check.process";
    public static final String ORDER_PAID_QUEUE = "kinetic.order.paid.queue";
    public static final String ORDER_REFUNDED_QUEUE = "kinetic.order.refunded.queue";
    public static final String ORDER_PAID_ROUTING_KEY = "order.paid";
    public static final String ORDER_REFUNDED_ROUTING_KEY = "order.refunded";

    @Bean
    public DirectExchange orderEventExchange() {
        return new DirectExchange(ORDER_EVENT_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange groupEventExchange() {
        return new DirectExchange(GROUP_EVENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderCloseDelayQueue(OrderBizProperties properties) {
        return QueueBuilder.durable(ORDER_CLOSE_DELAY_QUEUE)
                .ttl(properties.getCloseTimeoutMinutes() * 60 * 1000)
                .deadLetterExchange(ORDER_EVENT_EXCHANGE)
                .deadLetterRoutingKey(ORDER_CLOSE_PROCESS_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue orderCloseProcessQueue() {
        return QueueBuilder.durable(ORDER_CLOSE_PROCESS_QUEUE).build();
    }

    @Bean
    public Queue groupCheckDelayQueue() {
        return QueueBuilder.durable(GROUP_CHECK_DELAY_QUEUE)
                .deadLetterExchange(GROUP_EVENT_EXCHANGE)
                .deadLetterRoutingKey(GROUP_CHECK_PROCESS_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue groupCheckProcessQueue() {
        return QueueBuilder.durable(GROUP_CHECK_PROCESS_QUEUE).build();
    }

    @Bean
    public Queue orderPaidQueue() {
        return QueueBuilder.durable(ORDER_PAID_QUEUE).build();
    }

    @Bean
    public Queue orderRefundedQueue() {
        return QueueBuilder.durable(ORDER_REFUNDED_QUEUE).build();
    }

    @Bean
    public Binding orderCloseDelayBinding(@Qualifier("orderCloseDelayQueue") Queue orderCloseDelayQueue) {
        return BindingBuilder.bind(orderCloseDelayQueue)
                .to(orderEventExchange())
                .with(ORDER_CLOSE_DELAY_ROUTING_KEY);
    }

    @Bean
    public Binding orderCloseProcessBinding(@Qualifier("orderCloseProcessQueue") Queue orderCloseProcessQueue) {
        return BindingBuilder.bind(orderCloseProcessQueue)
                .to(orderEventExchange())
                .with(ORDER_CLOSE_PROCESS_ROUTING_KEY);
    }

    @Bean
    public Binding groupCheckDelayBinding(@Qualifier("groupCheckDelayQueue") Queue groupCheckDelayQueue) {
        return BindingBuilder.bind(groupCheckDelayQueue)
                .to(groupEventExchange())
                .with(GROUP_CHECK_DELAY_ROUTING_KEY);
    }

    @Bean
    public Binding groupCheckProcessBinding(@Qualifier("groupCheckProcessQueue") Queue groupCheckProcessQueue) {
        return BindingBuilder.bind(groupCheckProcessQueue)
                .to(groupEventExchange())
                .with(GROUP_CHECK_PROCESS_ROUTING_KEY);
    }

    @Bean
    public Binding orderPaidBinding(@Qualifier("orderPaidQueue") Queue orderPaidQueue) {
        return BindingBuilder.bind(orderPaidQueue)
                .to(orderEventExchange())
                .with(ORDER_PAID_ROUTING_KEY);
    }

    @Bean
    public Binding orderRefundedBinding(@Qualifier("orderRefundedQueue") Queue orderRefundedQueue) {
        return BindingBuilder.bind(orderRefundedQueue)
                .to(orderEventExchange())
                .with(ORDER_REFUNDED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
