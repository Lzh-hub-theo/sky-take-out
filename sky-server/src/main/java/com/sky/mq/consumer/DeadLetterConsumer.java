package com.sky.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.sky.dto.CartItemDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.MqReturnedMessage;
import com.sky.mapper.MqReturnedMessageMapper;
import com.sky.result.Result;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.sky.constant.RabbitMQConstant.*;
import static com.sky.constant.RedisKeyConstant.*;

@Component
@Slf4j
public class DeadLetterConsumer {
    @Autowired
    private RedisTemplate<String, String> strRedisTemplate;
    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;
    @Autowired
    private OrderService orderService;
    @Autowired
    private MqReturnedMessageMapper mqReturnedMessageMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final Duration TTL = Duration.ofMinutes(10);
    private static final Integer MAX_RETRY_COUNT = 3;

    @RabbitListener(queues = DEAD_LETTER_QUEUE, containerFactory = "batchRabbitListenerContainerFactory")
    public void consumeDeadLetterBatch(List<Message> messages, Channel channel) throws IOException {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        long lastDeliveryTag = 0;
        List<MqReturnedMessage> mqReturnedMessages = new ArrayList<>();

        for (Message message : messages) {
            String messageId = message.getMessageProperties().getMessageId();
            String redisKey = DLQ_DEDUPLICATE_PREFIX_KEY + messageId;
            String retryCountKey = DLQ_RETRY_KEY + messageId;

            try {
                // 1. 幂等性预检
                Boolean isFirstConsume = strRedisTemplate.opsForValue().setIfAbsent(redisKey, "1", TTL);
                redisTemplate.opsForValue().setIfAbsent(retryCountKey, 0, TTL);
                if (Boolean.FALSE.equals(isFirstConsume)) {
                    System.out.println("死信消息已处理过，跳过: " + messageId);
                    continue;
                }

                // 2. 执行业务逻辑（恢复库存、记录结果等）
                String body = new String(message.getBody());
                List<Map<String, ?>> xDeathHeader = message.getMessageProperties().getXDeathHeader();
                Map<String, ?> map = xDeathHeader.get(0);
                String reason = (String) map.get("reason");
                String queue = (String) map.get("queue");

                MqReturnedMessage mqReturnedMessage = MqReturnedMessage.builder()
                        .messageId(messageId)
                        .exchange("原队列：" + queue)
                        .routingKey("来自死信队列的消息")
                        .replyCode(300)
                        .replyText(reason)
                        .messageBody(body)
                        .build();
                mqReturnedMessages.add(mqReturnedMessage);

                // 恢复库存
                OrdersSubmitDTO ordersSubmitDTO = JSON.parseObject(body, OrdersSubmitDTO.class);
                List<CartItemDTO> cartItems = ordersSubmitDTO.getCartItems();
                orderService.restoreCacheStock(cartItems);

                // 返回消息处理结果
                String resultString = JSON.toJSONString(Result.error("下单失败，请重试"));
                strRedisTemplate.opsForValue().set(ORDER_TASK_RESULT_PREFIX_KEY + messageId, resultString, TTL);

            } catch (Exception e) {
                // 3. 发生异常，获取当前消息的重试次数
                Long retryCount = redisTemplate.opsForValue().increment(retryCountKey);
                redisTemplate.expire(retryCountKey, TTL);

                if (retryCount != null && retryCount < MAX_RETRY_COUNT) {
                    // 临时性故障：删除幂等记录，允许下次重试
                    strRedisTemplate.delete(redisKey);
                    rabbitTemplate.send(DEAD_LETTER_EXCHANGE, DEAD_LETTER_ROUTING_KEY, message);
                    System.err.println(String.format("消息 %s 处理失败，第 %d 次重试", messageId, retryCount));
                } else {
                    // 永久性故障：保留幂等记录，直接 ACK，防止死循环！
                    System.err.println(String.format("消息 %s 重试超限，确认为永久失败，直接丢弃并记录日志", messageId));

                }
            } finally {
                lastDeliveryTag = message.getMessageProperties().getDeliveryTag();
            }
        }

        // 批量落库
        if (mqReturnedMessages != null && !mqReturnedMessages.isEmpty()) {
            mqReturnedMessageMapper.insertBatch(mqReturnedMessages);
        }

        // 4. 批量手动 ACK
        // 无论是处理成功、重复消费，还是重试超限的“死刑”消息，都统一在这里确认
        channel.basicAck(lastDeliveryTag, true);
    }
}
