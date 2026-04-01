package com.sky.mapper;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 根据订单号修改订单状态
     * @param order
     */
    @Update("update orders set status=#{status}, pay_status=#{payStatus}, checkout_time=#{checkoutTime} where number=#{number}")
    void updateStatus(Orders order);

    /**
     * 条件查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    List<Orders> list(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据状态查询符合条件的订单数
     * @param map
     * @return
     */
    //@Select("select count(*) from orders where status = #{status}")
    Integer statisticsByStatus(Map map);

    /**
     * 根据订单号找到指定的订单信息
     * @param orderId
     * @return
     */
    @Select("select * from orders where id=#{orderId}")
    Orders getById(Long orderId);

    /**
     * 将过期的订单的状态设置为取消
     * @param time
     */
    @Update("update orders set status=6 where status = 1 and order_time < #{time}")
    void updateStatusToCancel(LocalDateTime time);

    /**
     * 将正在派送的订单的状态设置为完成
     */
    @Update("update orders set status = 5 where status = 4")
    void updateStatusToFinish();

    /**
     * 将在指定时间之后和指定状态的订单修改
     * @param orders
     * @param status
     * @param time
     */
    void updateByStatusAndOrderTimeLT(@Param("orders") Orders orders, @Param("status") Integer status, @Param("time") LocalDateTime time);


}
