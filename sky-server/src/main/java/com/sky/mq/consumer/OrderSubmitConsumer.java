package com.sky.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.sky.dto.OrderSubmitBaseDTO;
import com.sky.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.sky.constant.RabbitMQConstant.ORDER_QUEUE;

@Component
public class OrderSubmitConsumer {

    @Autowired
    private OrderService orderService;

    @RabbitListener(queues = ORDER_QUEUE)
    public void ReceiveOrder(String messageJson){
        System.out.println("收到下单消息");
        try{
            OrderSubmitBaseDTO orderSubmitBaseDTO = JSON.parseObject(messageJson, OrderSubmitBaseDTO.class);
            System.out.println(orderSubmitBaseDTO);
            orderService.submit(orderSubmitBaseDTO);
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("订单保存失败，准备重试");
        }
    }
}
