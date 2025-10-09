package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("select * from user where openid = #{openid}")
    public User selectUserByOpenId(String openid);

    public void insert(User user);

    @Select("select * from user where id=#{userId}")
    User getById(Long userId);

    @Select("select count(*) from user where cast(create_time as date) = #{createTime}")
    Integer statisticsByMap(Map map);
}
