package com.sky.task;

import com.alibaba.fastjson.JSON;
import com.sky.dto.CartItemDTO;
import com.sky.dto.OrdersSubmitBakDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.MqReturnedMessage;
import com.sky.entity.Orders;
import com.sky.mapper.MqReturnedMessageMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mq.producer.OrderSubmitProducer;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.sky.constant.RedisKeyConstant.EXCEPTION_MESSAGE_KEY;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private RedisTemplate<String, String> stringRedisTemplate;
    @Autowired
    private OrderSubmitProducer orderSubmitProducer;
    @Autowired
    private OrderService orderService;
    @Autowired
    private MqReturnedMessageMapper mqReturnedMessageMapper;

    /**
     * 处理超时订单
     */
    @Scheduled(cron = "0 * * * * ?")//每分钟触发一次
    //@Scheduled(cron = "1/5 * * * * ?")//调试用的
    public void processTimeOutOrder() {
        log.info("定时处理超时订单");
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        //orderMapper.updateStatusToCancel(time);

        Orders orders = new Orders();
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("订单超时，自动取消");
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.updateByStatusAndOrderTimeLT(orders, Orders.PENDING_PAYMENT, time);
    }

    /**
     * 处理派送中的异常订单
     */
    @Scheduled(cron = "0 0 1 * * ?")//每天凌晨一点触发一次
    //@Scheduled(cron = "0/5 * * * * ?")//调试用的
    public void processDeliveringOrder() {
        log.info("处理派送中的异常订单");
        //update orders set status = complete where status = delivery
        //orderMapper.updateStatusToFinish();

        //查询上一天的订单
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);

        Orders orders = new Orders();
        orders.setStatus(Orders.COMPLETED);

        orderMapper.updateByStatusAndOrderTimeLT(orders, Orders.DELIVERY_IN_PROGRESS, time);
    }

    @Scheduled(cron = "0 0/1 * * * ?")
    public void resendOrderMessage() {
        long now = System.currentTimeMillis() / 1000;
        long edge = now - 180;

        // 3分钟前的所有数据
        Set<String> expireMessageSet = stringRedisTemplate.opsForZSet().range(EXCEPTION_MESSAGE_KEY,0,edge);
        // 3分钟以内的所有数据
        Set<String> retryMessageSet = stringRedisTemplate.opsForZSet().range(EXCEPTION_MESSAGE_KEY, edge + 1, now);

        if(retryMessageSet!=null && !retryMessageSet.isEmpty()){
            for(String message: retryMessageSet){
                OrdersSubmitBakDTO ordersSubmitBakDTO = JSON.parseObject(message, OrdersSubmitBakDTO.class);
                Long userId = ordersSubmitBakDTO.getUserId();
                String messageId = ordersSubmitBakDTO.getMessageId();
                OrdersSubmitDTO ordersSubmitDTO = new OrdersSubmitDTO();
                BeanUtils.copyProperties(ordersSubmitBakDTO,ordersSubmitDTO);
                orderSubmitProducer.sendMessage(userId,messageId,ordersSubmitDTO);
            }
        }
        if(expireMessageSet!=null && !expireMessageSet.isEmpty()){
            List<MqReturnedMessage> list = new ArrayList<>();
            for (String message:expireMessageSet){
                OrdersSubmitBakDTO ordersSubmitBakDTO = JSON.parseObject(message, OrdersSubmitBakDTO.class);
                String messageId = ordersSubmitBakDTO.getMessageId();
                MqReturnedMessage mqReturnedMessage = MqReturnedMessage.builder()
                        .messageBody(message)
                        .messageId(messageId)
                        .replyText("无法访问到消息队列交换机")
                        .build();
                list.add(mqReturnedMessage);

                List<CartItemDTO> cartItems = ordersSubmitBakDTO.getCartItems();
                orderService.restoreCacheStock(cartItems);
            }
            stringRedisTemplate.opsForZSet().remove(EXCEPTION_MESSAGE_KEY,expireMessageSet.toArray());
            mqReturnedMessageMapper.insertBatch(list);
        }

    }
}
