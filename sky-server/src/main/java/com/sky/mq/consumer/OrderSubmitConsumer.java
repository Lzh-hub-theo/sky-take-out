package com.sky.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.sky.context.BaseContext;
import com.sky.dto.OrderSubmitBaseDTO;
import com.sky.mapper.IdempotencyMapper;
import com.sky.mq.producer.OrderSubmitProducer;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.sky.constant.RabbitMQConstant.*;
import static com.sky.constant.RedisKeyConstant.*;

@Component
@Slf4j
public class OrderSubmitConsumer {

    @Autowired
    private OrderService orderService;
    @Autowired
    private RedisTemplate<String, String> strRedisTemplate;
    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;
    @Autowired
    private IdempotencyMapper idempotencyMapper;
    @Autowired
    private OrderSubmitProducer orderSubmitProducer;

    private static final Duration TTL = Duration.ofMinutes(10);

    @RabbitListener(queues = ORDER_QUEUE, ackMode = "MANUAL")
    @Transactional
    public void idempotency(@Header(value = CONSUMER_ORDER_HEADER, required = false) Long userId,
                            Message message, Channel channel) throws IOException {
        String messageId = message.getMessageProperties().getMessageId();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        if (messageId == null) {
            // 缺少消息ID直接交给死信队列
//            log.error("调试信息：消息缺少 messageId");
            channel.basicNack(deliveryTag, false, false);
            return;
        }

        String deduplicateKey = DEDUPLICATE_PREFIX_KEY + messageId;
        Boolean isFirst = strRedisTemplate.opsForValue().setIfAbsent(deduplicateKey, "0", TTL);
        String status = strRedisTemplate.opsForValue().get(deduplicateKey);

        // redis拦截处理过的消息
        if (Boolean.FALSE.equals(isFirst) && "1".equals(status)) {
//            log.warn("调试信息，根据幂等性，Redis 拦截消费过的消息: {}", messageId);
            channel.basicAck(deliveryTag, false);
        } else {
            String retryKey = ORDER_QUEUE_RETRY_KEY + messageId;
            try {
                redisTemplate.opsForValue().setIfAbsent(retryKey, -1, TTL);
                // 数据库拦截处理过的消息
                idempotencyMapper.insert(messageId);
                this.processOrder(new String(message.getBody()), userId, messageId);
                idempotencyMapper.update(messageId);
                channel.basicAck(deliveryTag, false);
                strRedisTemplate.opsForValue().setIfAbsent(deduplicateKey, "1", TTL);
            } catch (DuplicateKeyException e) {
//                log.warn("调试信息：根据幂等性，数据库唯一键 拦截消费过的消息：{}", messageId);
                channel.basicAck(deliveryTag, false);
            } catch (Exception e) {
//                log.error("调试信息：业务处理失败: ", e);
                Long retryCount = redisTemplate.opsForValue().increment(retryKey);
                if (retryCount > 3) {
                    channel.basicNack(deliveryTag, false, false);
                } else {
                    orderSubmitProducer.sendMessage(userId, messageId, message);
                    channel.basicAck(deliveryTag, false);
                }
                throw new RuntimeException("消息消费失败", e);
            }
        }
    }

    private void processOrder(String messageJson, Long userId, String taskId) throws ArithmeticException {
//        int i = 1 / 0; // 模拟消息进入死信队列
        OrderSubmitBaseDTO orderSubmitBaseDTO = JSON.parseObject(messageJson, OrderSubmitBaseDTO.class);
//        log.info("调试信息，收到订单消息。taskId : {}", taskId);
        String key = ORDER_TASK_RESULT_PREFIX_KEY + taskId;

        if (userId == null) {
            Result<Object> result = Result.error("用户未登录");
            strRedisTemplate.opsForValue().set(key, JSON.toJSONString(result), 10, TimeUnit.MINUTES);
            return;
        }

        BaseContext.setCurrentId(userId);
        String resultString = null;
        try {
            OrderSubmitVO orderSubmitVO = orderService.submit(orderSubmitBaseDTO);
            resultString = JSON.toJSONString(Result.success(orderSubmitVO));
        } catch (Exception e) {
            resultString = JSON.toJSONString(Result.error(e.getMessage()));
        } finally {
            BaseContext.removeCurrentId();
        }
        strRedisTemplate.opsForValue().set(key, resultString, 10, TimeUnit.MINUTES);
    }
}
