package com.sky.config;

import com.alibaba.fastjson.JSON;
import com.sky.dto.OrdersSubmitBakDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.MqReturnedMessage;
import com.sky.mapper.MqReturnedMessageMapper;
import com.sky.mq.correlation.CustomCorrelationData;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.tools.RedisTool;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

import static com.sky.constant.RabbitMQConstant.*;
import static com.sky.constant.RedisKeyConstant.EXCEPTION_MESSAGE_KEY;
import static com.sky.constant.RedisKeyConstant.ORDER_TASK_RESULT_PREFIX_KEY;

@Configuration
public class RabbitMQConfiguration {
    @Bean
    public Queue orderQueue() {
        boolean durable = true;
        return new Queue(ORDER_QUEUE, durable);
    }

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue())
                .to(orderExchange())
                .with(ORDER_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MqReturnedMessageMapper mqReturnedMessageMapper,
                                         OrderService orderService,
                                         RedisTool redisTool,
                                         StringRedisTemplate stringRedisTemplate) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            // 拿到回调信息中的消息
            CustomCorrelationData customCorrelationData = (CustomCorrelationData) correlationData;
            String messageId = customCorrelationData.getId();
            Long userId = customCorrelationData.getUserId();
            String jsonString = customCorrelationData.getJsonMessageBody();
            OrdersSubmitBakDTO ordersSubmitBakDTO = JSON.parseObject(jsonString, OrdersSubmitBakDTO.class);
            ordersSubmitBakDTO.setMessageId(messageId);
            ordersSubmitBakDTO.setUserId(userId);
            String processedString = JSON.toJSONString(ordersSubmitBakDTO);
            if (!ack) {
                System.err.println("调试信息，消息发送到交换机失败：消息ID: " + messageId + "\n失败原因: " + cause);
                long timeStamp = System.currentTimeMillis() / 1000;
                redisTool.ZaddNx(EXCEPTION_MESSAGE_KEY, timeStamp, processedString);
            } else {
                System.out.println("调试信息：消息成功到达交换机，消息ID: " + messageId);
                stringRedisTemplate.opsForZSet().remove(EXCEPTION_MESSAGE_KEY, processedString);
            }
        });
        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            // 恢复库存
            String messageId = returnedMessage.getMessage().getMessageProperties().getMessageId();
            String jsonString = new String(returnedMessage.getMessage().getBody());
            OrdersSubmitDTO ordersSubmitDTO = JSON.parseObject(jsonString, OrdersSubmitDTO.class);
            orderService.restoreCacheStock(ordersSubmitDTO.getCartItems());

            // 消息存储到数据库中
            MqReturnedMessage mqReturnedMessage = MqReturnedMessage.builder()
                    .messageId(messageId)
                    .exchange(returnedMessage.getExchange())
                    .routingKey(returnedMessage.getRoutingKey())
                    .replyCode(returnedMessage.getReplyCode())
                    .replyText(returnedMessage.getReplyText())
                    .messageBody(new String(returnedMessage.getMessage().getBody()))
                    .build();
            mqReturnedMessageMapper.insert(mqReturnedMessage);

            // 返回下单失败
            String resultKey = ORDER_TASK_RESULT_PREFIX_KEY + messageId;
            jsonString = JSON.toJSONString(Result.error("下单失败，请重试"));
            stringRedisTemplate.opsForValue().set(resultKey,jsonString,10, TimeUnit.MINUTES);
        });
        return rabbitTemplate;
    }
}
