package com.sky.mq.producer;

import com.alibaba.fastjson.JSON;
import com.sky.context.BaseContext;
import com.sky.dto.OrderSubmitBaseDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.vo.OrderSubmitVO;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.beans.BeanUtils;
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
    private AmqpTemplate rabbitmqTemplate;
    @Autowired
    private RedisTemplate<String, String> strRedisTemplate;
    public String sendOrderMessage(OrdersSubmitDTO orderSubmitDTO) {
        OrderSubmitBaseDTO message = new OrderSubmitBaseDTO();
        BeanUtils.copyProperties(orderSubmitDTO,message);
        String taskId = UUID.randomUUID().toString();
        message.setTaskId(taskId);
        try {
            // 设置请求头
            Long userId = BaseContext.getCurrentId();
            MessagePostProcessor messagePostProcessor = msg -> {
                if(userId != null){
                    msg.getMessageProperties().setHeader(CONSUMER_ORDER_HEADER, userId);
                }
                return msg;
            };
            // 将对象序列化为 JSON 字符串发送到消息队列
            String messageJson = JSON.toJSONString(message);
            rabbitmqTemplate.convertAndSend(
                    ORDER_EXCHANGE,
                    ORDER_ROUTING_KEY,
                    messageJson,
                    messagePostProcessor
            );
            System.out.println("消息发送成功: " + messageJson);

            // 存入缓存
            String pollKey = ORDER_TASK_RESULT_PREFIX_KEY + taskId;
            OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                    .orderStatus(0)
                    .build();
            String orderSubmitVOString = JSON.toJSONString(orderSubmitVO);
            strRedisTemplate.opsForValue().set(pollKey, orderSubmitVOString, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return taskId;
    }
}
