package com.sky.controller.user;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.sky.constant.RedisKeyConstant.ORDER_TASK_RESULT_PREFIX_KEY;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "订单接口")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private RedisTemplate<String, String> strRedisTemplate;

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO 提交订单请求类
     * @return 暂时什么都不返回
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<String> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户下单:{}", ordersSubmitDTO);
        String taskId = orderService.processOrders(ordersSubmitDTO);
        return Result.needPoll(taskId);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 历史订单查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询")
    public Result<PageResult> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("历史订单查询:{}", ordersPageQueryDTO);
        PageResult list = orderService.listWithDetails(ordersPageQueryDTO);
        return Result.success(list);
    }

    /**
     * 查询订单详情
     *
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> orderDetail(@PathVariable Long id) {
        log.info("查询订单详情:{}", id);
        OrderVO orderVO = orderService.getOrderDetail(id);
        return Result.success(orderVO);
    }

    /**
     * 取消订单
     *
     * @param id
     * @return
     */
    @ApiOperation("取消订单")
    @PutMapping("/cancel/{id}")
    public Result cancelOrder(@PathVariable Long id) {
        log.info("取消订单:{}", id);
        orderService.cancelOrder(id);
        return Result.success();
    }

    /**
     * 再来一单
     *
     * @param id
     * @return
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable Long id) {
        log.info("再来一单:{}", id);
        orderService.repitition(id);
        return Result.success();
    }

    /**
     * 催单
     *
     * @param id 订单id
     */
    @GetMapping("/reminder/{id}")
    @ApiOperation("催单")
    public Result remind(@PathVariable Long id) {
        log.info("催单");
        orderService.remind(id);
        return Result.success();
    }

    /**
     * 获取预计送达时间
     * @param params 店铺ID和收货地址
     * @return 预计送达时间字符串 (格式: yyyy-MM-dd HH:mm:ss)
     */
    @GetMapping("/getEstimatedDeliveryTime")
    public Result<String> getEstimatedDeliveryTime(@Valid EstimatedDeliveryTimeDTO params) {
        String estimatedTime = orderService.calculateEstimatedDeliveryTime(params);
        return Result.success(estimatedTime);
    }

    @GetMapping("/order/status/{taskId}")
    public Result<Object> getOrderStatusResult(@PathVariable String taskId){
        System.out.println("调试："+taskId);
        String key = ORDER_TASK_RESULT_PREFIX_KEY + taskId;
        String resultString = strRedisTemplate.opsForValue().get(key);
        return JSON.parseObject(resultString, new TypeReference<Result<Object>>() {});
    }
}
