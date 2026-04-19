package com.sky.constant;

public final class RabbitMQConstant {
    private RabbitMQConstant(){}

    public static final String ORDER_QUEUE = "order.submit.queue";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_ROUTING_KEY = "order.submit.key";
}
