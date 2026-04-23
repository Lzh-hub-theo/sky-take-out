package com.sky.mq.producer;

import com.alibaba.fastjson.JSON;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.exception.BaseException;
import com.sky.mq.correlation.CustomCorrelationData;
import com.sky.result.Result;
import com.sky.vo.OrderSubmitVO;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.sky.constant.RabbitMQConstant.*;
import static com.sky.constant.RedisKeyConstant.ORDER_TASK_RESULT_PREFIX_KEY;

@Component
public class OrderSubmitProducer {
    @Autowired
    private RabbitTemplate rabbitmqTemplate;
    @Autowired
    private RedisTemplate<String, String> strRedisTemplate;

    public String sendOrderMessage(OrdersSubmitDTO orderSubmitDTO) {
        // 参数校验
        Long userId = BaseContext.getCurrentId();
        if (userId == null) throw new BaseException("用户未登录");
        String taskId = UUID.randomUUID().toString();

        try {
            // 发送消息
            sendMessage(userId, taskId, orderSubmitDTO);
            // 存入缓存
            saveResultToCache(taskId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return taskId;
    }

    public void sendMessage(Long userId, String messageId, OrdersSubmitDTO message){
        // 设置请求头
        MessagePostProcessor messagePostProcessor = msg -> {
            msg.getMessageProperties().setHeader(CONSUMER_ORDER_HEADER, userId);
            msg.getMessageProperties().setMessageId(messageId);
            return msg;
        };

        // 将对象序列化为 JSON 字符串发送到消息队列
        String messageJson = JSON.toJSONString(message);

        // 设置 messageId 到 CorrelationData
        CustomCorrelationData correlationData = new CustomCorrelationData(messageId, userId, messageJson);

        rabbitmqTemplate.convertAndSend(
                ORDER_EXCHANGE,
                ORDER_ROUTING_KEY,
                messageJson,
                messagePostProcessor,
                correlationData
        );
//        // 模拟无法到达交换机
//        rabbitmqTemplate.convertAndSend(
//                "fake_exchange",
//                ORDER_ROUTING_KEY,
//                messageJson,
//                messagePostProcessor,
//                correlationData
//        );
//        // 模拟交换机无法到达消息队列
//        rabbitmqTemplate.convertAndSend(
//                ORDER_EXCHANGE,
//                "fake_routing_key",
//                messageJson,
//                messagePostProcessor,
//                correlationData
//        );
        System.out.println("消息发送成功: " + messageJson);

    }

    private void saveResultToCache(String taskId) {
        String pollKey = ORDER_TASK_RESULT_PREFIX_KEY + taskId;
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .orderStatus(0)
                .build();
        Result<OrderSubmitVO> result = Result.needPoll(orderSubmitVO);
        String resultString = JSON.toJSONString(result);
        strRedisTemplate.opsForValue().set(pollKey, resultString, 10, TimeUnit.MINUTES);
    }
}
