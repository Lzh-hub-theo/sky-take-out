package com.sky.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface IdempotencyMapper {
    @Insert("insert into mq_consume_log (message_id) values (#{messageId})")
    void insert(String messageId);

    @Update("update mq_consume_log set status = 1 where message_id = #{messageId}")
    void update(String messageId);
}
