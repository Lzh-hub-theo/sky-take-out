package com.sky.controller.user;

import com.sky.dto.ShopInfoDTO;
import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.sky.constant.RedisKeyConstant.SHOP_STATUS_KEY;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Slf4j
@Api(tags="店铺相关接口")
public class ShopController {
    @Autowired
    private RedisTemplate<String, Integer> shopStatusRedisTemplate;

    @Autowired
    private ShopService shopService;

    /**
     * 获取营业状态
     * @return
     */
    @ApiOperation("获取营业状态")
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        Integer shopStatus = shopStatusRedisTemplate.opsForValue().get(SHOP_STATUS_KEY);
        shopStatus = shopStatus == null ? 0 : shopStatus;
        log.info("获取营业状态:{}",shopStatus == 1?"营业中":"打烊中");
        return Result.success(shopStatus);
    }

    /**
     * 获取店铺联系方式等信息
     */
    @GetMapping("/getMerchantInfo")
    public Result<ShopInfoDTO> getMerchantInfo() {
        ShopInfoDTO shopInfo = shopService.getMerchantInfo();
        return Result.success(shopInfo);
    }
}
