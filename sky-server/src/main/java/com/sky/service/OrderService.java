package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    PageResult listWithDetails(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderVO getOrderDetail(Long orderId);

    /**
     * 用户端取消订单
     * @param id
     */
    void cancelOrder(Long id);

    void repitition(Long id);

    /**
     * 各个状态的订单数量统计
     * @return
     */
    OrderStatisticsVO statistics();

    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 管理端拒单
     * @param ordersCancelDTO
     */
    void cancel(OrdersCancelDTO ordersCancelDTO);

    /**
     * 商家端取消订单
     * @param ordersCancelDTO
     */
    void cancelOrderByAdmin(OrdersCancelDTO ordersCancelDTO);

    void modifyStatusToDelivery(Long id);

    void modifyStatusToComplete(Long id);

    void remind(Long id);

    String calculateEstimatedDeliveryTime(EstimatedDeliveryTimeDTO params);

}
