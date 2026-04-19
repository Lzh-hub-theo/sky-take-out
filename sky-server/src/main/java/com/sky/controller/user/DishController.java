package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.sky.constant.RedisKeyConstant.DISH_CATEGORY_PREFIX_KEY;

@Api(tags = "菜品浏览接口")
@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
public class DishController {

    @Autowired
    DishService dishService;
    @Autowired
    private RedisTemplate<String, Object> jsonRedisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId 分类id
     * @return 菜品列表
     */
    @ApiOperation("根据分类id查询菜品")
    @GetMapping("/list")
    public Result<List<DishVO>> queryByCategoryId(Long categoryId) {
        log.info("根据分类id查询菜品:{}", categoryId);
        String key = DISH_CATEGORY_PREFIX_KEY + categoryId;
        List<DishVO> dishes = (List<DishVO>) jsonRedisTemplate.opsForValue().get(key);

        if (dishes != null && dishes.size() > 0) {
            return Result.success(dishes);
        }

        dishes = dishService.queryByCategoryIdAdam(categoryId);
        jsonRedisTemplate.opsForValue().set(key, dishes);
        jsonRedisTemplate.expire(key, 1, TimeUnit.HOURS);
        return Result.success(dishes);
    }
}
