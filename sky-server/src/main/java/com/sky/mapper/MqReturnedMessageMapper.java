package com.sky.mapper;

import com.sky.entity.MqReturnedMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MqReturnedMessageMapper {

    @Insert("insert into mq_returned_message (message_id, exchange, routing_key, reply_code, reply_text, message_body) " +
            "VALUES (#{messageId}, #{exchange}, #{routingKey}, #{replyCode}, #{replyText}, #{messageBody})")
    boolean insert(MqReturnedMessage mqReturnedMessage);

    boolean insertBatch(List<MqReturnedMessage> mqReturnedMessagesList);
}
