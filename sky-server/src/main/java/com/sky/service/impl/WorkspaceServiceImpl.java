package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.mapper.*;
import com.sky.result.Result;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 查询今日运营数据
     * @return
     */
    @Override
    @Transactional
    public BusinessDataVO businessData(LocalDate begin,LocalDate end) {
        //有效订单数
        Integer validOrderNumber = orderMapper.statisticsByStatus(Map.of("status", Orders.COMPLETED, "begin", begin, "end", end));

        //订单完成率
        //总订单数
        Integer totalOrderNumber = orderMapper.statisticsByStatus(Map.of("begin", begin, "end", end));
        Double completeRatio;
        if(totalOrderNumber==0) completeRatio=0.0;
        else completeRatio=(double)validOrderNumber/(double)totalOrderNumber;

        //新增用户数
        Integer newUserNumber = userMapper.statisticsByMap(Map.of("begin", begin, "end", end));

        //营业额
        List<SubtractTurnoverVO> turnoverList = reportMapper.getTurnoversByBeginAndEnd(begin,end);
        Double turnover = 0.0;
        if(turnoverList.size()!=0) {
            for(SubtractTurnoverVO turnoverVO:turnoverList) {
                turnover += turnoverVO.getTurnover();
            }
        }

        //平均客单价
        Double unitPrice;
        if(validOrderNumber==0) unitPrice=0.0;
        else unitPrice = turnover / validOrderNumber;

        BusinessDataVO businessDataVO = BusinessDataVO.builder()
                .turnover(turnover)
                .orderCompletionRate(completeRatio)
                .validOrderCount(validOrderNumber)
                .newUsers(newUserNumber)
                .unitPrice(unitPrice)
                .build();
        return businessDataVO;
    }

    /**
     * 查询套餐总览
     * @return
     */
    @Override
    public SetmealOverViewVO overviewSetmeals() {
        Integer enable = setmealMapper.queryCountByStatus(StatusConstant.ENABLE);
        Integer disable = setmealMapper.queryCountByStatus(StatusConstant.DISABLE);
        return SetmealOverViewVO.builder().sold(enable).discontinued(disable).build();
    }

    /**
     * 查询菜品总览
     * @return
     */
    @Override
    public DishOverViewVO overviewDishes() {
        Integer enable = dishMapper.queryCountByStatus(StatusConstant.ENABLE);
        Integer disable = dishMapper.queryCountByStatus(StatusConstant.DISABLE);
        return DishOverViewVO.builder().sold(enable).discontinued(disable).build();
    }

    @Override
    public OrderOverViewVO overviewOrders() {
        LocalDate now = LocalDate.now();
        //全部订单
        Integer allOrders = orderMapper.statisticsByStatus(Map.of("checkoutTime", now));
        //已取消数量
        Integer cancelOrders = orderMapper.statisticsByStatus(Map.of("checkoutTime", now, "status", Orders.CANCELLED));
        //已完成数量
        Integer completedOrders = orderMapper.statisticsByStatus(Map.of("checkoutTime", now, "status", Orders.COMPLETED));
        //待派送数量
        Integer deliveryOrders = orderMapper.statisticsByStatus(Map.of("checkoutTime", now, "status", Orders.DELIVERY_IN_PROGRESS));
        //待接单数量
        Integer waitConfirmedOrders = orderMapper.statisticsByStatus(Map.of("checkoutTime", now, "status", Orders.TO_BE_CONFIRMED));

        return OrderOverViewVO.builder().allOrders(allOrders).cancelledOrders(cancelOrders).completedOrders(completedOrders)
                .deliveredOrders(deliveryOrders).waitingOrders(waitConfirmedOrders).build();
    }


}
