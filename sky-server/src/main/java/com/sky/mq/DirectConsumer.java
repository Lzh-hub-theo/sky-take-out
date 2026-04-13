package com.sky.mq;

import com.rabbitmq.client.*;

import java.util.Map;

public class DirectConsumer {

    private static final String EXCHANGE_NAME = "direct_exchange";
    private static final String QUEUE_NAME1 = "multi_color_queue";
    private static final String QUEUE_NAME2 = "single_color_queue";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // 创建两个队列
        Map<String, Object> args = Map.of("x-queue-type", "quorum");
        channel.queueDeclare(QUEUE_NAME1, true, false, false, args);

        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        String queueName = "multi_color_queue";

        channel.queueBind(queueName, EXCHANGE_NAME, "blue");
        channel.queueBind(queueName, EXCHANGE_NAME, "red");
        channel.queueBind(queueName, EXCHANGE_NAME, "yellow");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [queue1] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });

        // ===================

        String queueName2 = "single_color_queue";
        channel.queueDeclare(QUEUE_NAME2, true, false, false, args);
        channel.queueBind(queueName2, EXCHANGE_NAME, "blue");
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [queue2] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel.basicConsume(queueName2, true, deliverCallback2, consumerTag -> { });
    }
}