package com.sky.mq.producer;

import com.alibaba.fastjson.JSON;
import com.sky.dto.OrderSubmitBaseDTO;
import com.sky.dto.OrdersSubmitDTO;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.sky.constant.RabbitMQConstant.ORDER_EXCHANGE;
import static com.sky.constant.RabbitMQConstant.ORDER_ROUTING_KEY;

@Component
public class OrderSubmitProducer {
    @Autowired
    private AmqpTemplate rabbitmqTemplate;
    public void sendOrderMessage(OrdersSubmitDTO orderSubmitDTO) {
        OrderSubmitBaseDTO message = new OrderSubmitBaseDTO();
        BeanUtils.copyProperties(orderSubmitDTO,message);
        try {
            // 将对象序列化为 JSON 字符串发送
            String messageJson = JSON.toJSONString(message);

            rabbitmqTemplate.convertAndSend(
                    ORDER_EXCHANGE,
                    ORDER_ROUTING_KEY,
                    messageJson
            );
            System.out.println("消息发送成功: " + messageJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
