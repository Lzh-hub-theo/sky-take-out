package com.sky.handler;

import com.alibaba.fastjson.JSON;
import com.sky.dto.CartItemDTO;
import com.sky.dto.OrdersSubmitBakDTO;
import com.sky.entity.MqReturnedMessage;
import com.sky.mapper.MqReturnedMessageMapper;
import com.sky.mq.correlation.CustomCorrelationData;
import com.sky.result.Result;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.sky.constant.RedisKeyConstant.ORDER_TASK_RESULT_PREFIX_KEY;

@Component
@Slf4j
public class MessageExceptionHandler {
    @Autowired
    private OrderService orderService;
    @Autowired
    private MqReturnedMessageMapper mqReturnedMessageMapper;
    @Autowired
    private RedisTemplate<String, String> stringRedisTemplate;

    // 仅用于存放异常消息的内存队列，由后台线程批量刷入 Redis/DB
    private final BlockingQueue<String> exceptionMessageQueue = new LinkedBlockingQueue<>(10000);

    @PostConstruct
    public void startBatchPersistence() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            List<String> batch = new ArrayList<>();
            exceptionMessageQueue.drainTo(batch, 200);
            if (!batch.isEmpty()) {
                try {
                    List<MqReturnedMessage> list = new ArrayList<>();
                    for (String message:batch){
                        // 拼接数据到列表中
                        OrdersSubmitBakDTO ordersSubmitBakDTO = JSON.parseObject(message, OrdersSubmitBakDTO.class);
                        String messageId = ordersSubmitBakDTO.getMessageId();
                        MqReturnedMessage mqReturnedMessage = MqReturnedMessage.builder()
                                .messageId(messageId)
                                .exchange("无法路由到交换机")
                                .routingKey("无法路由到交换机")
                                .replyCode(1000)
                                .replyText("无法访问到消息队列交换机")
                                .messageBody(message)
                                .build();
                        list.add(mqReturnedMessage);

                        // 恢复缓存
                        List<CartItemDTO> cartItems = ordersSubmitBakDTO.getCartItems();
                        orderService.restoreCacheStock(cartItems);

                        // redis存入错误消息
                        String resultKey = ORDER_TASK_RESULT_PREFIX_KEY + messageId;
                        String jsonString = JSON.toJSONString(Result.error("π_π 下单失败，请重试"));
                        stringRedisTemplate.opsForValue().set(resultKey,jsonString,10, TimeUnit.MINUTES);
                    }
                    // 批量插入数据库
                    mqReturnedMessageMapper.insertBatch(list);
                } catch (Exception e) {
                    log.error("批量持久化异常消息失败", e);
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public void offer(CustomCorrelationData customCorrelationData) {
        String messageId = customCorrelationData.getId();
        Long userId = customCorrelationData.getUserId();
        String jsonString = customCorrelationData.getJsonMessageBody();
        OrdersSubmitBakDTO ordersSubmitBakDTO = JSON.parseObject(jsonString, OrdersSubmitBakDTO.class);
        ordersSubmitBakDTO.setMessageId(messageId);
        ordersSubmitBakDTO.setUserId(userId);
        String processedString = JSON.toJSONString(ordersSubmitBakDTO);
        // 非阻塞入队，队列满则直接丢弃（或记录死信）
        exceptionMessageQueue.offer(processedString);
    }
}