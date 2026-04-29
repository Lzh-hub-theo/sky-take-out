package com.sky.config;

import com.alibaba.fastjson.JSON;
import com.sky.dto.OrdersSubmitBakDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.MqReturnedMessage;
import com.sky.mapper.MqReturnedMessageMapper;
import com.sky.mq.correlation.CustomCorrelationData;
import com.sky.properties.RabbitMQProperties;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.tools.RedisTool;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static com.sky.constant.RabbitMQConstant.*;
import static com.sky.constant.RedisKeyConstant.EXCEPTION_MESSAGE_KEY;
import static com.sky.constant.RedisKeyConstant.ORDER_TASK_RESULT_PREFIX_KEY;

@Configuration
public class RabbitMQConfiguration {

    @Autowired
    private RabbitMQProperties rabbitMQProperties;

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(rabbitMQProperties.getMq().getPrefetch());
        return factory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory batchRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setBatchListener(true);
        factory.setConsumerBatchEnabled(true);
        factory.setBatchSize(rabbitMQProperties.getDlq().getBatchSize());
        factory.setPrefetchCount(rabbitMQProperties.getDlq().getPrefetch());
        return factory;
    }

    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(ORDER_QUEUE)
                .withArgument("x-message-ttl", 600000)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
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
    public Queue dlq() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE)
                .build();
    }

    @Bean
    public DirectExchange dlx() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(dlq())
                .to(dlx())
                .with(DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MqReturnedMessageMapper mqReturnedMessageMapper,
                                         OrderService orderService,
                                         RedisTool redisTool,
                                         StringRedisTemplate stringRedisTemplate,
                                         @Qualifier("orderAsyncExecutor")Executor asyncExecutor) {
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
//                System.err.println("调试信息，消息发送到交换机失败：消息ID: " + messageId + "\n失败原因: " + cause);
                long timeStamp = System.currentTimeMillis() / 1000;
                CompletableFuture.runAsync(() ->
                        redisTool.ZaddNx(EXCEPTION_MESSAGE_KEY, timeStamp, processedString),
                        asyncExecutor);
            } else {
//                System.out.println("调试信息：消息成功到达交换机，消息ID: " + messageId);
                CompletableFuture.runAsync(() ->
                        stringRedisTemplate.opsForZSet().remove(EXCEPTION_MESSAGE_KEY, processedString),
                        asyncExecutor);
            }
        });

        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            // 恢复库存
            String messageId = returnedMessage.getMessage().getMessageProperties().getMessageId();
            String jsonString = new String(returnedMessage.getMessage().getBody());
            OrdersSubmitDTO ordersSubmitDTO = JSON.parseObject(jsonString, OrdersSubmitDTO.class);
            CompletableFuture.runAsync(() ->
                    orderService.restoreCacheStock(ordersSubmitDTO.getCartItems()),
                    asyncExecutor);

            // 消息存储到数据库中
            MqReturnedMessage mqReturnedMessage = MqReturnedMessage.builder()
                    .messageId(messageId)
                    .exchange(returnedMessage.getExchange())
                    .routingKey(returnedMessage.getRoutingKey())
                    .replyCode(returnedMessage.getReplyCode())
                    .replyText(returnedMessage.getReplyText())
                    .messageBody(new String(returnedMessage.getMessage().getBody()))
                    .build();
            CompletableFuture.runAsync(() ->
                    mqReturnedMessageMapper.insert(mqReturnedMessage),
                    asyncExecutor);

            // 返回下单失败
            String resultKey = ORDER_TASK_RESULT_PREFIX_KEY + messageId;
            String string = JSON.toJSONString(Result.error("下单失败，请重试"));
            CompletableFuture.runAsync(() ->
                    stringRedisTemplate.opsForValue().set(resultKey, string, 10, TimeUnit.MINUTES),
                    asyncExecutor);

        });

        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }
}
