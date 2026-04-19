package com.sky.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.sky.context.BaseContext;
import com.sky.dto.OrderSubmitBaseDTO;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.sky.constant.RabbitMQConstant.CONSUMER_ORDER_HEADER;
import static com.sky.constant.RabbitMQConstant.ORDER_QUEUE;
import static com.sky.constant.RedisKeyConstant.ORDER_TASK_RESULT_PREFIX_KEY;

@Component
@Slf4j
public class OrderSubmitConsumer {

    @Autowired
    private OrderService orderService;
    @Autowired
    private RedisTemplate<String, String> strRedisTemplate;

    @RabbitListener(queues = ORDER_QUEUE)
    public void ReceiveOrder(String messageJson, @Header(value = CONSUMER_ORDER_HEADER, required = false) Long userId) {
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
