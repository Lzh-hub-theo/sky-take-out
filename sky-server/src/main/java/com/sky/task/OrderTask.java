package com.sky.task;

import com.sky.entity.Orders;
import com.sky.handler.MessageExceptionHandler;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.sky.constant.RedisKeyConstant.EXCEPTION_MESSAGE_KEY;
import static com.sky.constant.RedisKeyConstant.ORDER_TASK_RESULT_PREFIX_KEY;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private MessageExceptionHandler messageExceptionHandler;

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
    public void processDeliveringOrder() {
        log.info("处理派送中的异常订单");

        //查询上一天的订单
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);

        Orders orders = new Orders();
        orders.setStatus(Orders.COMPLETED);

        orderMapper.updateByStatusAndOrderTimeLT(orders, Orders.DELIVERY_IN_PROGRESS, time);
    }

    @Scheduled(fixedRate = 5000, initialDelay = 5000)
    public void doScheduledTask(){
        messageExceptionHandler.startBatchPersistence();
    }

//    @Scheduled(cron = "0 0/1 * * * ?")
//    public void resendOrderMessage() {
//        long now = System.currentTimeMillis() / 1000;
//        long edge = now - 180;
//
//        // 3分钟前的所有数据
//        Set<String> expireMessageSet = stringRedisTemplate.opsForZSet().rangeByScore(EXCEPTION_MESSAGE_KEY,0,edge);
//        // 3分钟以内的所有数据
//        Set<String> retryMessageSet = stringRedisTemplate.opsForZSet().rangeByScore(EXCEPTION_MESSAGE_KEY, edge + 1, now);
//
//        if(retryMessageSet!=null && !retryMessageSet.isEmpty()){
//            // 重新发送消息
//            for(String message: retryMessageSet){
//                OrdersSubmitBakDTO ordersSubmitBakDTO = JSON.parseObject(message, OrdersSubmitBakDTO.class);
//                Long userId = ordersSubmitBakDTO.getUserId();
//                String messageId = ordersSubmitBakDTO.getMessageId();
//                OrdersSubmitDTO ordersSubmitDTO = new OrdersSubmitDTO();
//                BeanUtils.copyProperties(ordersSubmitBakDTO,ordersSubmitDTO);
//                orderSubmitProducer.sendMessage(userId,messageId,ordersSubmitDTO);
//            }
//        }
//        if(expireMessageSet!=null && !expireMessageSet.isEmpty()){
//            List<MqReturnedMessage> list = new ArrayList<>();
//            for (String message:expireMessageSet){
//                // 拼接数据到列表中
//                OrdersSubmitBakDTO ordersSubmitBakDTO = JSON.parseObject(message, OrdersSubmitBakDTO.class);
//                String messageId = ordersSubmitBakDTO.getMessageId();
//                MqReturnedMessage mqReturnedMessage = MqReturnedMessage.builder()
//                        .messageId(messageId)
//                        .exchange("无法路由到交换机")
//                        .routingKey("无法路由到交换机")
//                        .replyCode(1000)
//                        .replyText("无法访问到消息队列交换机")
//                        .messageBody(message)
//                        .build();
//                list.add(mqReturnedMessage);
//
//                // 恢复缓存
//                List<CartItemDTO> cartItems = ordersSubmitBakDTO.getCartItems();
//                orderService.restoreCacheStock(cartItems);
//
//                // redis存入错误消息
//                String resultKey = ORDER_TASK_RESULT_PREFIX_KEY + messageId;
//                String jsonString = JSON.toJSONString(Result.error("π_π 下单失败，请重试"));
//                stringRedisTemplate.opsForValue().set(resultKey,jsonString,10, TimeUnit.MINUTES);
//            }
//            // 批量删除缓存、批量插入数据库
//            stringRedisTemplate.opsForZSet().remove(EXCEPTION_MESSAGE_KEY,expireMessageSet.toArray());
//            mqReturnedMessageMapper.insertBatch(list);
//        }
//
//    }
}
