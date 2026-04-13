package com.sky.mq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Scanner;

public class TopicProducer {

    private static final String EXCHANGE_NAME = "topic_exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(EXCHANGE_NAME, "topic");

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String[] strings = scanner.nextLine().split(" ");
                String message = strings[0];
                String routingKey = strings[1];
                AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                        .expiration("60000")
                        .build();
                channel.basicPublish(EXCHANGE_NAME, routingKey, properties, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");
            }
        }
    }
}