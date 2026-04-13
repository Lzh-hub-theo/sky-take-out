package com.sky.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.Map;

public class TopicConsumer {

    private static final String QUEUE1_NAME = "topic_queue_1";
    private static final String QUEUE2_NAME = "topic_queue_2";
    private final static String EXCHANGE_NAME = "topic_exchange";
    private static final String DLX_EXCHANGE_NAME = "dlx_exchange";

    private static final String[] BINDING_KEYS={"*.orange.*","*.*.rabbit","lazy.#"};

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        Map<String, Object> args = Map.of("x-queue-type", "quorum");
        channel.queueDeclare(QUEUE1_NAME, true, false, false, args);
        channel.queueDeclare(QUEUE2_NAME, true, false, false, args);

        channel.exchangeDeclare(EXCHANGE_NAME, "topic");

        channel.queueBind(QUEUE1_NAME, EXCHANGE_NAME, BINDING_KEYS[0]);
        channel.queueBind(QUEUE2_NAME, EXCHANGE_NAME, BINDING_KEYS[1]);
        channel.queueBind(QUEUE2_NAME, EXCHANGE_NAME, BINDING_KEYS[2]);

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // 遇到处理不了的就给死信交换机
        channel.exchangeDeclare(DLX_EXCHANGE_NAME,"direct");

        // 消费者
        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            if(message.contains("cat")){
                channel.basicPublish(DLX_EXCHANGE_NAME, "cat", null, message.getBytes("UTF-8"));
                return;
            }
            System.out.println(" [consumer1] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel.basicConsume(QUEUE1_NAME, true, deliverCallback1, consumerTag -> {
        });

        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            if(message.contains("dog")){
                channel.basicPublish(DLX_EXCHANGE_NAME, "dog", null, message.getBytes("UTF-8"));
                return;
            }
            System.out.println(" [consumer2] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel.basicConsume(QUEUE2_NAME, true, deliverCallback2, consumerTag -> {
        });
    }
}