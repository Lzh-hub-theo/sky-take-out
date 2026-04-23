package com.sky.tools;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisTool {
    @Autowired
    private RedisTemplate<String, String> stringRedisTemplate;

    public Boolean ZaddNx(String key, Long score, String member) {
        return stringRedisTemplate.execute((RedisCallback<Boolean>) connection -> {
            byte[] keyBytes = stringRedisTemplate.getStringSerializer().serialize(key);
            byte[] memberBytes = stringRedisTemplate.getStringSerializer().serialize(member);
            byte[] scoreBytes = stringRedisTemplate.getStringSerializer().serialize(String.valueOf(score));

            Object rawResult = connection.execute("ZADD", keyBytes, "NX".getBytes(), scoreBytes, memberBytes);

            if(rawResult instanceof Long){
                return ((Long) rawResult) == 1;
            }
            return false;
        });
    }
}
