package com.sky.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.sky.context.BaseContext;
import com.sky.dto.OrderSubmitBaseDTO;
import com.sky.mapper.IdempotencyMapper;
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

import static com.sky.constant.RabbitMQConstant.CONSUMER_ORDER_HEADER;
import static com.sky.constant.RabbitMQConstant.ORDER_QUEUE;
import static com.sky.constant.RedisKeyConstant.DEDUPLICATE_PREFIX_KEY;
import static com.sky.constant.RedisKeyConstant.ORDER_TASK_RESULT_PREFIX_KEY;

@Component
@Slf4j
public class OrderSubmitConsumer {

    @Autowired
    private OrderService orderService;
    @Autowired
    private RedisTemplate<String, String> strRedisTemplate;
    @Autowired
    private IdempotencyMapper idempotencyMapper;

    private static final Duration TTL = Duration.ofMinutes(10);

    @RabbitListener(queues = ORDER_QUEUE, ackMode = "MANUAL")
    @Transactional
    public void idempotency(@Header(value = CONSUMER_ORDER_HEADER, required = false) Long userId,
                            Message message, Channel channel) throws IOException {
        String messageId = message.getMessageProperties().getMessageId();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        if (messageId == null) {
            log.error("调试信息：消息缺少 messageId");
            channel.basicAck(deliveryTag, false);
            return;
        }

        String deduplicateKey = DEDUPLICATE_PREFIX_KEY + messageId;
        Boolean isFirst = strRedisTemplate.opsForValue().setIfAbsent(deduplicateKey, "1", TTL);

        if (Boolean.TRUE.equals(isFirst)) {
            try {
                idempotencyMapper.insert(messageId);
                this.processOrder(new String(message.getBody()), userId);
                idempotencyMapper.update(messageId);
                channel.basicAck(deliveryTag, false);
            } catch (DuplicateKeyException e) {
                log.warn("调试信息：消息已处理：{}", messageId);
                channel.basicAck(deliveryTag, false);
            } catch (Exception e) {
                log.error("调试信息：业务处理失败: ", e);
                channel.basicNack(deliveryTag, false, true);
            }
        } else {
            log.warn("调试信息，Redis 拦截重复消息: {}", messageId);
            channel.basicAck(deliveryTag, false);
        }
    }

    private void processOrder(String messageJson, Long userId) {
        OrderSubmitBaseDTO orderSubmitBaseDTO = JSON.parseObject(messageJson, OrderSubmitBaseDTO.class);
        String taskId = orderSubmitBaseDTO.getTaskId();
        log.info("调试信息，收到订单消息。taskId : {}", taskId);
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
