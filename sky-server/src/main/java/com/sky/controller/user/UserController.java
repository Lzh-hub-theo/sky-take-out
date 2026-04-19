package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.sky.constant.RedisKeyConstant.DISH_STOCK_KEY;

@RestController
@RequestMapping("/user/user")
@Api(tags = "C端用户相关接口")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 用户登录接口
     * @param userLoginDTO
     */
    @PostMapping("/login")
    @ApiOperation("用户登录接口")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){
        log.info("用户登录接口:{}", userLoginDTO);
        User user = userService.wxLogin(userLoginDTO);

        Map<String,Object> claims=new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String jwt = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(jwt)
                .build();

        return Result.success(userLoginVO);
    }

    @Autowired
    private RedisTemplate<String, String> strRedisTemplate;

    @PostMapping("/test")
    public Result<Integer> testRedisOpsForHash(){
        String key = DISH_STOCK_KEY;
        String hashKey = String.valueOf(1);
        strRedisTemplate.opsForHash().put(key,hashKey,28);
        Integer dishStock = (Integer) strRedisTemplate.opsForHash().get(key, hashKey);
        strRedisTemplate.expire(key, 1, TimeUnit.HOURS);
        return Result.success(dishStock);
    }
}
