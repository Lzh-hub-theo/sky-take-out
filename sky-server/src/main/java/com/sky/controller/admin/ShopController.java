package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import static com.sky.constant.RedisKeyConstant.SHOP_STATUS_KEY;

@Slf4j
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
public class ShopController {
    @Autowired
    private RedisTemplate<String, Integer> shopStatusRedisTemplate;

    /**
     * 设置营业状态
     * Integer status
     *
     * @return
     */
    @ApiOperation("设置营业状态")
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status) {
        log.info("设置营业状态:{}", status == 1 ? "营业中" : "打样中");

        shopStatusRedisTemplate.opsForValue().set(SHOP_STATUS_KEY, status);

        return Result.success();
    }

    /**
     * 获取营业状态
     *
     * @return
     */
    @ApiOperation("获取营业状态")
    @GetMapping("/status")
    public Result<Integer> getStatus() {
        log.info("获取营业状态");

        Integer status = shopStatusRedisTemplate.opsForValue().get(SHOP_STATUS_KEY);

        return Result.success(status);
    }
}
