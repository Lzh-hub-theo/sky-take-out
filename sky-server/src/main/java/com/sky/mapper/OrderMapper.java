package com.sky.mapper;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

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

    @Update("update orders set status=#{status}, pay_status=#{payStatus}, checkout_time=#{checkoutTime} where number=#{number}")
    void updateStatus(Orders order);

    List<Orders> list(Orders orders, LocalDateTime begin, LocalDateTime end);

    @Select("select couunt(*) from orders where status = #{status}")
    Integer statisticsByStatus(Integer status);
}
