package com.sky.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;

public class DlxConsumer {
    private final static String QUEUE_NAME1 = "dlx_queue1";
    private final static String QUEUE_NAME2 = "dlx_queue2";
    private final static String EXCHANGE_NAME = "dlx_exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //死信交换机
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        //处理猫的死信队列
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-dead-letter-exchange", EXCHANGE_NAME);
        args.put("x-dead-letter-routing-key", "cat");
        channel.queueDeclare(QUEUE_NAME1, true, false, false, args);

        //处理狗的死信队列
        args.put("x-dead-letter-routing-key", "dog");
        channel.queueDeclare(QUEUE_NAME2, true, false, false, args);

        //绑定
        channel.queueBind(QUEUE_NAME1,EXCHANGE_NAME,"cat");
        channel.queueBind(QUEUE_NAME2,EXCHANGE_NAME,"dog");

        // 消费猫
        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [cat Consumer] Received '" + message + "'");
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        channel.basicConsume(QUEUE_NAME1, false, deliverCallback1, consumerTag -> {
        });

        // 消费狗
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [dog Consumer] Received '" + message + "'");
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        channel.basicConsume(QUEUE_NAME2, false, deliverCallback2, consumerTag -> {
        });

    }
}
