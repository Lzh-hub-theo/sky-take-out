package com.sky.initialization;

import com.sky.entity.DishStock;
import com.sky.mapper.DishMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.sky.constant.RedisKeyConstant.DISH_STOCK_KEY;

@Component
public class DishStockLoader {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private RedisTemplate<String, String> stockRedisTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void loadStock(){
        Map<Long, DishStock> dishStockMap = dishMapper.getDishStockMap();
        Map<String, String> map = dishStockMap.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> String.valueOf(entry.getKey()),
                        entry -> String.valueOf(entry.getValue().getStock())
                ));

        stockRedisTemplate.opsForHash().putAll(DISH_STOCK_KEY, map);
        stockRedisTemplate.expire(DISH_STOCK_KEY,1, TimeUnit.HOURS);
    }

}
